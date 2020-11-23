package org.oddjob.scheduling;


import org.oddjob.*;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.framework.extend.BasePrimary;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.framework.util.StopWait;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.persist.Persistable;
import org.oddjob.scheduling.state.TimerState;
import org.oddjob.scheduling.state.TimerStateAdapter;
import org.oddjob.scheduling.state.TimerStateChanger;
import org.oddjob.scheduling.state.TimerStateHandler;
import org.oddjob.state.*;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;
import org.oddjob.util.Restore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Common functionality for jobs that schedule things.
 * 
 * @author Rob Gordon
 */
public abstract class ScheduleBase extends BasePrimary
implements 
		Runnable, Stoppable, Serializable,
        Resettable, Stateful, Structural, Forceable {
	private static final long serialVersionUID = 2009031500L;
	
	/** Fires state events. */
	protected transient volatile TimerStateHandler stateHandler;
	
	/** Used to notify clients of an icon change. */
	private transient volatile IconHelper iconHelper;
	
	/** Used to state change states and icons. */
	private transient volatile TimerStateChanger stateChanger;
	
	/** Track the child. */
	protected transient volatile ChildHelper<Runnable> childHelper; 
			
	/** Track structural state changes. */
	private transient volatile Stateful structuralState;
			
	protected transient volatile StateExchange<TimerState> childStateReflector;
		
	/** Stop flag. */
	protected transient volatile boolean stop;
	
	protected transient volatile CountDownLatch begun;
	
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
		stateHandler = new TimerStateHandler(this);
		childHelper = new ChildHelper<>(this);
		structuralState = new TimerStateAdapter(
				new StructuralStateHelper(childHelper, getStateOp()));
		
		iconHelper = new IconHelper(this, 
				StateIcons.iconFor(stateHandler.getState()));
		
		stateChanger = new TimerStateChanger(stateHandler, iconHelper, 
				new Persistable() {					
					@Override
					public void persist() throws ComponentPersistException {
						save();
					}
				});
		
		childStateReflector = new StateExchange<>(structuralState,
				new OrderedStateChanger<>(stateChanger, stateHandler));
	}

	@Override
	protected TimerStateHandler stateHandler() {
		return stateHandler;
	}
	
	@Override
	protected IconHelper iconHelper() {
		return iconHelper;
	}
	
	protected StateChanger<TimerState> getStateChanger() {
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
	 * @throws ComponentPersistException If the scheduled time can't be saved.
	 */
	abstract protected void begin() throws ComponentPersistException;

	/**
	 * Implement the main execute method for a job. This surrounds the 
	 * doExecute method of the sub class and sets state for the job.
	 */
	@Override
	public final void run() {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					stop = false;
					childStateReflector.stop();
					
					getStateChanger().setState(TimerState.STARTING);					
				}
			})) {
				return;
			}
			
			logger().info("Executing.");

			try {
				configure();
				
				// Used to ensure consistent states.
				begun = new CountDownLatch(1);
				
				begin();
				
				// rescheduling could already have set the state so only
				// change it if we are still executing.
				stateHandler.waitToWhen(StateConditions.EXECUTING, 
						new Runnable() {
					@Override
					public void run() {
						getStateChanger().setState(TimerState.STARTED);
					}
				});
				
			begun.countDown();
			}
			catch (final Throwable e) {
				logger().warn("Job Exception:", e);
				
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						getStateChanger().setStateException(e);
					}
				});
			}	
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
	@Override
	public final void stop() throws FailedToStopException {
		stateHandler.assertAlive();

		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			final AtomicReference<String> lastIcon = new AtomicReference<>();
			
			if (stateHandler.waitToWhen(new IsStoppable(), new Runnable() {				
				@Override
				public void run() {
					logger().info("Stopping.");
					
					stop = true;
					
					stateHandler.wake();
					
					lastIcon.set(iconHelper.currentId());
					iconHelper.changeIcon(IconHelper.STOPPING);					
				}
			})) {
				
				// cancel future executions for timer. remove listener for trigger.
				onStop();
				
				// then stop children
				try {
					childHelper.stopChildren();		
					
					postStop();
					
					new StopWait(this).run();
				}
				catch (FailedToStopException e) {
					iconHelper.changeIcon(lastIcon.get());
					logger().warn("Failed to stop", e);
				}
				
				logger().info("Stopped.");
			}
			else {
				
				childHelper.stopChildren();		
			}
		} 
	}
	
	/**
	 * Subclasses can override to perform stopping operations.
	 */
	protected void onStop() {
		
	}
	
	/**
	 * Subclasses can override to perform actions once children have stopped.
	 */
	protected void postStop() {
		
	}
	
	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {
					logger().debug("Propagating Soft Reset to children.");			

					childStateReflector.stop();
					childHelper.softResetChildren();

					onReset();

					getStateChanger().setState(TimerState.STARTABLE);

					logger().info("Soft Reset complete.");			
				}
			});
		}
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
				public void run() {
					
					logger().debug("Propagating Hard Reset to children.");			
					
					childStateReflector.stop();
					childHelper.hardResetChildren();
					
					onReset();
					
					getStateChanger().setState(TimerState.STARTABLE);
		
					logger().info("Hard Reset complete.");			
				}
			});
		}
	}

	/**
	 * Override by subclasses to reset state.
	 */
	protected void onReset() {
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.Forceable#force()
	 */
	@Override
	public void force() {
		
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			stateHandler.waitToWhen(new IsForceable(), new Runnable() {
				public void run() {
					logger().info("Forcing complete.");			
					
					getStateChanger().setState(TimerState.COMPLETE);
				}
			});
		} 
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
		stateHandler.assertAlive();
		
		super.onDestroy();
		
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			logger().info("Destroying.");
			
			stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
				public void run() {
					childStateReflector.stop();
					stop = true;
					if (stateHandler.getState().isStoppable()) {
						onStop();
						stateChanger.setState(TimerState.STARTABLE);
					}
				}					
			});
		} 
	}
	
	/**
	 * Internal method to fire state.
	 */
	protected void fireDestroyedState() {
		
		if (!stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				stateHandler().setState(TimerState.DESTROYED);
				stateHandler().fireEvent();
			}
		})) {
			throw new IllegalStateException("[" + ScheduleBase.this + "] Failed set state DESTROYED");
		}
		logger().debug("[" + this + "] Destroyed.");				
	}
}
