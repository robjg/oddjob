package org.oddjob.framework;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.oddjob.FailedToStopException;
import org.oddjob.Forceable;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.persist.Persistable;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.OrderedStateChanger;
import org.oddjob.state.ParentState;
import org.oddjob.state.ParentStateChanger;
import org.oddjob.state.ParentStateHandler;
import org.oddjob.state.State;
import org.oddjob.state.StateChanger;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateExchange;
import org.oddjob.state.StateOperator;
import org.oddjob.state.StructuralStateHelper;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;
import org.oddjob.util.Restore;

/**
 * An abstract implementation of a job which provides common functionality to
 * concrete sub classes.
 * 
 * @author Rob Gordon
 */
public abstract class StructuralJob<E> extends BasePrimary
implements 
		Runnable, Serializable, 
		Stoppable, Resetable, Stateful, Forceable, Structural {
	private static final long serialVersionUID = 2009031500L;
	
	/** Handle state. */
	private transient volatile ParentStateHandler stateHandler;
	
	/** Used to notify clients of an icon change. */
	private transient volatile IconHelper iconHelper;
	
	/** Track changes to children an notify listeners. */
	protected transient volatile ChildHelper<E> childHelper; 
			
	/** Calculate our state based on children. */
	protected transient volatile StructuralStateHelper structuralState;
		
	/** Reflect state of children. */
	protected transient volatile 
	StateExchange<ParentState> childStateReflector;
	
	private transient volatile ParentStateChanger stateChanger;
	
	/**
	 * @oddjob.property
	 * @oddjob.description Read only view of the internal stop flag. 
	 * This flag is cleared with a reset.
	 * @oddjob.required Read only.
	 */
	protected transient volatile boolean stop;
		
	protected transient volatile boolean destroy;
	
	/**
	 * Constructor.
	 */
	public StructuralJob() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		stateHandler = new ParentStateHandler(this);		
		childHelper = new ChildHelper<E>(this);
		structuralState = new StructuralStateHelper(childHelper, 
				getInitialStateOp());
		iconHelper = new IconHelper(this, 
				StateIcons.iconFor(stateHandler.getState()));
		stateChanger = new ParentStateChanger(stateHandler, iconHelper, 
				new Persistable() {					
					@Override
					public void persist() throws ComponentPersistException {
						save();
					}
				});
		childStateReflector = new StateExchange<ParentState>(structuralState, 
				new OrderedStateChanger<ParentState>(stateChanger, stateHandler));
	}
		
	@Override
	protected ParentStateHandler stateHandler() {
		return stateHandler;
	}

	@Override
	protected IconHelper iconHelper() {
		return iconHelper;
	}
	
	protected final StateChanger<ParentState> getStateChanger() {
		return stateChanger;
	}
	
	/**
	 * Subclasses must provide the {@link StateOperator} that will decide
	 * how to evaluate the children's state.
	 * 
	 * @return A State Operator. Must not be null.
	 */
	abstract protected StateOperator getInitialStateOp();
	
	/**
	 * Execute this job.
	 * 
	 * @throws Exception If the unexpected occurs.
	 */
	abstract protected void execute() throws Throwable;

	/**
	 * Implement the main execute method for a job. This surrounds the 
	 * doExecute method of the sub class and sets state for the job.
	 */
	public final void run() {
		try (Restore restore = ComponentBoundry.push(loggerName(), this)) {		
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					// it's possible to reset children and then execute again so this
					// is just in case there was no reset.
					childStateReflector.stop();
					
					getStateChanger().setState(ParentState.EXECUTING);
				}					
			})) {
				return;
			}
			
			logger().info("Executing.");

			try {
				if (!stop) {
					configure();
				}
				
				if (!stop) {
					execute();
				}
				
				// we ignore state while executing but now we need to update.
				// dependent on our child states.
				startChildStateReflector();
			}
			catch (final Throwable e) {
				logger().error("Job Exception.", e);
				
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						getStateChanger().setStateException(e);
					}
				});
			}	
			logger().info("Execution finished.");
		}
	}
	
	/**
	 * Start the child state reflector. Sub classes override this if they
	 * need to start the child state reflector at a different time.
	 */
	protected void startChildStateReflector() {
		if (!destroy) {
			childStateReflector.start();
			logger().debug("Child State Reflector Started.");
		}
	}
	
	/**
	 * Implementation for a typical stop. 
	 * <p>
	 * This stop implementation doesn't check that the job is 
	 * executing as stop messages must cascade down the hierarchy
	 * to manually started jobs.
	 * 
	 * @throws FailedToStopException 
	 */
	public void stop() throws FailedToStopException {
		stateHandler.assertAlive();
		
		try (Restore restore = ComponentBoundry.push(loggerName(), this)) {		
			if (stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
					logger().info("Stopping.");
					
					stop = true;
					
					stateHandler.wake();
					
					iconHelper.changeIcon(IconHelper.STOPPING);
				}					
			})) {
				// Order is here for SimultaneousStructural to cancel jobs first.
				
				onStop();
				
				childHelper.stopChildren();
				
				new StopWait(this).run();
				
				stop = false;
				
				logger().info("Stopped.");		
			}
			else {
				
				childHelper.stopChildren();
			}
		} 
	}
	
	/**
	 * Allow sub classes to do something on stop.
	 */
	protected void onStop() throws FailedToStopException { }
	
	
	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		try (Restore restore = ComponentBoundry.push(loggerName(), this)) {		
			return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {
				
					logger().debug("Propagating Soft Reset to children.");			
					
					childStateReflector.stop();
					childHelper.softResetChildren();
					stop = false;
					onSoftReset();
					getStateChanger().setState(ParentState.READY);
					
					logger().info("Soft Reset complete.");
				}
			});	
		}
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		
		try (Restore restore = ComponentBoundry.push(loggerName(), this)) {		
			return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
				public void run() {
					logger().debug("Propagating Hard Reset to children.");			
					
					childStateReflector.stop();
					childHelper.hardResetChildren();
					stop = false;
					onHardReset();
					getStateChanger().setState(ParentState.READY);
					
					logger().info("Hard Reset complete.");
				}
			});
		}
	}

	/**
	 * Allow sub classes to do something on HARD reset.
	 */
	protected void onHardReset() {
		onReset();
	}
	
	/**
	 * Allow sub classes to do something on SOFT reset.
	 */
	protected void onSoftReset() {
		onReset();
	}
	
	
	/**
	 * Allow sub classes to do something on reset.
	 */
	protected void onReset() {
		
	}
	
	/**
	 * Force the job to COMPLETE.
	 */
	@Override
	public void force() {
		
		try (Restore restore = ComponentBoundry.push(loggerName(), this)) {		
			stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {
					logger().info("Forcing complete.");			
					
					childStateReflector.stop();
					
					getStateChanger().setState(ParentState.COMPLETE);
				}
			});
		}
	}

	/**
	 * Add a listener. The listener will immediately receive add
	 * notifications for all existing children.
	 * 
	 * @param listener The listener.
	 */	
	public void addStructuralListener(StructuralListener listener) {
		stateHandler.assertAlive();
		
		childHelper.addStructuralListener(listener);
	}
	
	/**
	 * Remove a listener.
	 * 
	 * @param listener The listener.
	 */
	public void removeStructuralListener(StructuralListener listener) {
		childHelper.removeStructuralListener(listener);
	}	
			
	/**
	 * Expose the internal stop flag as a read only property.
	 * 
	 * @return the stop flag.
	 */
	public boolean isStop() {
		return stop;
	}
	
	/**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
		s.writeObject(getName());
		if (loggerName().startsWith(getClass().getName())) {
			s.writeObject(null);
		}
		else {
			s.writeObject(loggerName());
		}
		s.writeObject(stateHandler.lastStateEvent().serializable());
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		String name = (String) s.readObject();
		logger((String) s.readObject());
		StateEvent.SerializableNoSource savedEvent = 
				(StateEvent.SerializableNoSource) s.readObject();
		
		completeConstruction();
		
		setName(name);
		stateHandler.restoreLastJobStateEvent(savedEvent);
		iconHelper.changeIcon(
				StateIcons.iconFor(stateHandler.getState()));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		logger().info("Destroying.");
		
		stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				destroy = true;
				stop = true;
				
				childStateReflector.stop();

				stateHandler.wake();
	
				State state = stateHandler.getState();
				
				// This is here to allow Asynchronous jobs to cancel
				// pending tasks.
				if (state.isStoppable()) {
					try {
						onStop();
					} 
					catch (FailedToStopException e) {
						logger().warn("Failed to stop during destroy.", e);
					}
				}
			}					
		});			
	}
	
	/**
	 * Internal method to fire state.
	 */
	protected void fireDestroyedState() {
		
		if (!stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				stateHandler().setState(ParentState.DESTROYED);
				stateHandler().fireEvent();
			}
		})) {
			throw new IllegalStateException("[" + StructuralJob.this + "] Failed set state DESTROYED");
		}
		logger().debug("[" + this + "] Destroyed.");				
	}
}
