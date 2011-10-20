package org.oddjob.framework;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.oddjob.FailedToStopException;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.persist.Persistable;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.OrderedStateChanger;
import org.oddjob.state.ParentState;
import org.oddjob.state.ParentStateChanger;
import org.oddjob.state.ParentStateHandler;
import org.oddjob.state.StateChanger;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateExchange;
import org.oddjob.state.StateOperator;
import org.oddjob.state.StructuralStateHelper;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * An abstract implementation of a job which provides common functionality to
 * concrete sub classes.
 * 
 * @author Rob Gordon
 */

public abstract class StructuralJob<E> extends BasePrimary
implements 
		Runnable, Serializable, 
		Stoppable, Resetable, Stateful, Structural {
	private static final long serialVersionUID = 2009031500L;
	
	protected transient ParentStateHandler stateHandler;
	
	/** Track changes to children an notify listeners. */
	protected transient ChildHelper<E> childHelper; 
			
	/** Calculate our state based on children. */
	protected transient StructuralStateHelper structuralState;
		
	/** Reflect state of children. */
	protected transient StateExchange childStateReflector;
	
	private transient ParentStateChanger stateChanger;
	
	protected transient volatile boolean stop;
	
	/**
	 * Constructor.
	 */
	public StructuralJob() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		stateHandler = new ParentStateHandler(this);		
		childHelper = new ChildHelper<E>(this);
		structuralState = new StructuralStateHelper(childHelper, getStateOp());
		stateChanger = new ParentStateChanger(stateHandler, iconHelper, 
				new Persistable() {					
					@Override
					public void persist() throws ComponentPersistException {
						save();
					}
				});
		childStateReflector = new StateExchange(structuralState, 
				new OrderedStateChanger<ParentState>(stateChanger, stateHandler));
	}
		
	@Override
	protected ParentStateHandler stateHandler() {
		return stateHandler;
	}
	
	protected StateChanger<ParentState> getStateChanger() {
		return stateChanger;
	}
	
	abstract protected StateOperator getStateOp();
	
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
		OddjobNDC.push(loggerName(), this);
		try {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					childStateReflector.stop();
					
					getStateChanger().setState(ParentState.EXECUTING);
				}					
			})) {
				return;
			}
			
			logger().info("Executing.");

			try {
				configure();
				
				execute();
				
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
		finally {
			OddjobNDC.pop();
		}
	}
	
	protected void startChildStateReflector() {
		childStateReflector.start();
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
		
		OddjobNDC.push(loggerName(), this);
		try {

			if (!stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
				public void run() {
					stop = true;
					
					logger().info("Stopping.");
					
					stateHandler.wake();
					
					iconHelper.changeIcon(IconHelper.STOPPING);
				}					
			})) {
				throw new IllegalStateException();
			}
	
			FailedToStopException failedToStopException = null;
			try {
				// Order is here for SimultaneousStructural to cancel jobs first.
				
				onStop();
				
				childHelper.stopChildren();
				
			} catch (FailedToStopException e) {
				failedToStopException = e;
			} catch (RuntimeException e) {
				failedToStopException =
					new FailedToStopException(StructuralJob.this, e);
			}				
			
			try {
				if (failedToStopException == null) {				
	
					new StopWait(this).run();
					
					logger().info("Stopped.");		
				}
				else {
					throw failedToStopException;
				}
			}	finally {	
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						iconHelper.changeIcon(
								StateIcons.iconFor(stateHandler.getState()));
					}					
				});
			}		
		} finally {
			OddjobNDC.pop();
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
		OddjobNDC.push(loggerName(), this);
		try {
			return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {
				
					logger().debug("Propergating Soft Reset to children.");			
					
					childStateReflector.stop();
					childHelper.softResetChildren();
					stop = false;
					onReset();
					getStateChanger().setState(ParentState.READY);
					
					logger().info("Soft Reset complete.");
				}
			});	
		} finally {
			OddjobNDC.pop();
		}
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		
		OddjobNDC.push(loggerName(), this);
		try {
			return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
				public void run() {
					logger().debug("Propergating Hard Reset to children.");			
					
					childStateReflector.stop();
					childHelper.hardResetChildren();
					onReset();
					stop = false;
					getStateChanger().setState(ParentState.READY);
					
					logger().info("Hard Reset complete.");
				}
			});
		} finally {
			OddjobNDC.pop();
		}
	}

	/**
	 * Allow sub classes to do something on reset.
	 */
	protected void onReset() {
		
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
		s.writeObject(stateHandler.lastStateEvent());
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		String name = (String) s.readObject();
		logger((String) s.readObject());
		StateEvent savedEvent = (StateEvent) s.readObject();
		
		completeConstruction();
		
		setName(name);
		stateHandler.restoreLastJobStateEvent(savedEvent);
		iconHelper.changeIcon(
				StateIcons.iconFor(stateHandler.getState()));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		try {
			stop();
		} catch (FailedToStopException e) {
			logger().warn(e);
		}
		
		childStateReflector.stop();
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
