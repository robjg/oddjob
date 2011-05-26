package org.oddjob.framework;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import org.oddjob.FailedToStopException;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.OrderedStateChanger;
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
	
	/** Track changes to children an notify listeners. */
	protected transient ChildHelper<E> childHelper; 
			
	/** Calculate our state based on children. */
	protected transient StructuralStateHelper structuralState;
		
	/** Reflect state of children. */
	protected transient StateExchange childStateReflector;
	
	/**
	 * Constructor.
	 */
	public StructuralJob() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		childHelper = new ChildHelper<E>(this);
		structuralState = new StructuralStateHelper(childHelper, getStateOp());
		childStateReflector = new StateExchange(structuralState, 
				new OrderedStateChanger(getStateChanger(), stateHandler));
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
		OddjobNDC.push(loggerName());
		try {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					childStateReflector.stop();
					
					getStateChanger().setJobState(JobState.EXECUTING);
				}					
			})) {
				return;
			}
			
			logger().info("[" + StructuralJob.this + "] Executing.");

			try {
				configure();
				
				execute();
				// we ignore state while executing but now we need to update.
				// dependent on our child states.
				childStateReflector.start();
			}
			catch (final Throwable e) {
				logger().error("[" + StructuralJob.this + "] Job Exception.", e);
				
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						getStateChanger().setJobStateException(e);
					}
				});
			}	
			logger().info("[" + StructuralJob.this + "] Execution finished.");
		}
		finally {
			OddjobNDC.pop();
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
		
		final AtomicReference<FailedToStopException> failedToStop = 
			new AtomicReference<FailedToStopException>();
		
		if (!stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				stop = true;
				
				logger().info("[" + StructuralJob.this + "] Stop requested.");
				
				stateHandler.wake();
				
				iconHelper.changeIcon(IconHelper.STOPPING);
				try {
					onStop();
				} catch (FailedToStopException e) {
					failedToStop.set(e);
				} catch (RuntimeException e) {
					failedToStop.set(
							new FailedToStopException(StructuralJob.this, e));
				}				
			}					
		})) {
			throw new IllegalStateException();
		}

		if (failedToStop.get() == null) {
			try {
				childHelper.stopChildren();
			} catch (FailedToStopException e) {
				failedToStop.set(e);
			} catch (RuntimeException e) {
				failedToStop.set(
						new FailedToStopException(StructuralJob.this, e));
			}				
		}
		
		FailedToStopException e = failedToStop.get();
		try {
			if (e == null) {				

				new StopWait(this).run();
				
				logger().info("[" + StructuralJob.this + "] Stopped.");		
			}
			else {
				throw e;
			}
		}	finally {	
			stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
				public void run() {
					iconHelper.changeIcon(
							StateIcons.iconFor(stateHandler.getJobState()));
				}					
			});
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
		return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
			public void run() {
			
				logger().debug("[" + StructuralJob.this + "] Propergating Soft Reset to children.");			
				
				childStateReflector.stop();
				childHelper.softResetChildren();
				stop = false;
				onReset();
				getStateChanger().setJobState(JobState.READY);
				
				logger().info("[" + StructuralJob.this + "] Soft Reset.");
			}
		});	
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		
		return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
			public void run() {
				logger().debug("[" + StructuralJob.this + "] Propergating Hard Reset to children.");			
				
				childStateReflector.stop();
				childHelper.hardResetChildren();
				onReset();
				stop = false;
				getStateChanger().setJobState(JobState.READY);
				
				logger().info("[" + StructuralJob.this + "] Hard Reset.");
			}
		});
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
		s.writeObject(stateHandler.lastJobStateEvent());
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		setName((String) s.readObject());
		logger((String) s.readObject());
		JobStateEvent savedEvent = (JobStateEvent) s.readObject();
		stateHandler.restoreLastJobStateEvent(savedEvent);
		iconHelper.changeIcon(
				StateIcons.iconFor(stateHandler.getJobState()));
		completeConstruction();
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
}
