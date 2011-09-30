package org.oddjob.scheduling;


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
import org.oddjob.framework.BasePrimary;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.logging.OddjobNDC;
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
import org.oddjob.state.StateChanger;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateExchange;
import org.oddjob.state.StateOperator;
import org.oddjob.state.StructuralStateHelper;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * Common functionality for jobs that schedule things.
 * 
 * @author Rob Gordon
 */

public abstract class ScheduleBase extends BasePrimary
implements 
		Runnable, Stoppable, Serializable, 
		Resetable, Stateful, Structural {
	private static final long serialVersionUID = 2009031500L;
	
	/** Fires state events. */
	private transient ParentStateHandler stateHandler;
	
	/** Used to state change states and icons. */
	private transient ParentStateChanger stateChanger;
	
	/** Track the child. */
	protected transient ChildHelper<Runnable> childHelper; 
			
	protected transient StructuralStateHelper structuralState;
			
	protected transient StateExchange childStateReflector;
		
	/** Stop flag. */
	protected transient volatile boolean stop;
	
	/**
	 * Default Constructor.
	 */
	public ScheduleBase() {
		completeConstruction();
	}
	
	/**
	 * Common construction.
	 */
	private void completeConstruction() {
		stateHandler = new ParentStateHandler(this);
		childHelper = new ChildHelper<Runnable>(this);
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
		
	/**
	 * Sub classes provide the state operator that is used to calculate the subclasses 
	 * completion state.
	 *  
	 * @return The operator. Must not be null.
	 */
	abstract protected StateOperator getStateOp();
	
	/**
	 * Sub classes must override this to submit the first execution.
	 * 
	 * @throws ComponentPerisistException If the scheduled time can't be saved.
	 */
	abstract protected void begin() throws ComponentPersistException;

	/**
	 * Implement the main execute method for a job. This surrounds the 
	 * doExecute method of the sub class and sets state for the job.
	 */
	public final void run() {
		OddjobNDC.push(loggerName());
		try {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					stop = false;
					childStateReflector.stop();
					
					getStateChanger().setState(ParentState.EXECUTING);					
				}
			})) {
				return;
			}
			logger().info("Executing job [" + ScheduleBase.this + "]");

			try {
				configure();
				
				// set icon to sleeping now because begin might
				// set it to executing and we don't want to override
				// this.
				iconHelper.changeIcon(IconHelper.SLEEPING);
				
				begin();
				
				stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
					public void run() {
						stateHandler.setState(ParentState.ACTIVE);
						stateHandler.fireEvent();
					}
				});
			}
			catch (final Throwable e) {
				logger().warn("[" + ScheduleBase.this + "] Job Exception:", e);
				
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						getStateChanger().setStateException(e);
					}
				});
			}	
		}
		finally {
			OddjobNDC.pop();
		}
	}
	
	/**
	 * Implementation for a typical stop. Subclasses must implement 
	 * Stoppable to take advantage of it.
	 * <p>
	 * This stop implementation doesn't check that the job is 
	 * executing as stop messages must cascade down the hierarchy
	 * to manually started jobs.
	 * @throws FailedToStopException 
	 */
	public final void stop() throws FailedToStopException {
		stateHandler.assertAlive();

		if (!stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
			@Override
			public void run() {
				stop = true;
				stateHandler.wake();
			}
		})) {
			return;
		}
		
		logger().info("[" + this + "] Stop requested.");
		
		String lastIcon = iconHelper.currentId();
		iconHelper.changeIcon(IconHelper.STOPPING);
		
		// cancel future executions for timer. remove listener for trigger.
		onStop();
		
		// then stop children
		try {
			childHelper.stopChildren();		
		}
		catch (FailedToStopException e) {
			iconHelper.changeIcon(lastIcon);
			logger().warn(e);
		}
		
		stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				getStateChanger().setState(ParentState.READY);
			}
		});
		
		logger().info("[" + this + "] Stopped.");
	}
	
	protected void onStop() {
		
	}
	
	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
			public void run() {
				logger().debug("[" + ScheduleBase.this + "] Propergating Soft Reset to children.");			
				
				childStateReflector.stop();
				childHelper.softResetChildren();
				
				onReset();
				
				getStateChanger().setState(ParentState.READY);
				
				logger().info("[" + ScheduleBase.this + "] Soft Reset.");			
			}
		});
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
			public void run() {
				
				logger().debug("[" + ScheduleBase.this + "] Propergating Hard Reset to children.");			
				
				childStateReflector.stop();
				childHelper.hardResetChildren();
				
				onReset();
				
				getStateChanger().setState(ParentState.READY);
	
				logger().info("[" + ScheduleBase.this + "] Hard Reset.");			
			}
		});
	}

	protected void onReset() {
		
	}
	
	
	/**
	 * Add a listener. The listener will immediately recieve add
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
	 * @param listener The listner.
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
			throw new IllegalStateException("[" + ScheduleBase.this + " Failed set state DESTROYED");
		}
		logger().debug("[" + ScheduleBase.this + "] destroyed.");				
	}
}
