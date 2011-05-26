package org.oddjob.jobs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import org.oddjob.FailedToStopException;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.BasePrimary;
import org.oddjob.framework.StopWait;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.scheduling.Keeper;
import org.oddjob.scheduling.LoosingOutcome;
import org.oddjob.scheduling.Outcome;
import org.oddjob.scheduling.WinningOutcome;
import org.oddjob.state.AbstractJobStateListener;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;
import org.oddjob.state.OrderedStateChanger;
import org.oddjob.state.StateChanger;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * @oddjob.description Grab work to do. By competing for work with
 * other Grabbers this job facilitates distribution of work between
 * multiple Oddjob processes.
 * <p>
 *
 * @oddjob.example
 * 
 * See the user guide.
 * 
 * @author rob
 *
 */
public class GrabJob extends BasePrimary 
implements
		Runnable, Serializable, 
		Stoppable, Resetable, Stateful, Structural {
	private static final long serialVersionUID = 2010031800L;

	/**
	 * Actions on loosing.
	 */
	public enum LoosingAction {
		COMPLETE,
		INCOMPLETE,
		WAIT,
	}
	
	/**
     * @oddjob.property
     * @oddjob.description The action on loosing. Available actions are:
     * <dl>
     * 	<dt>COMPLETE</dt>
     * 	<dd>Set the job state to COMPLETE.</dd>
     *  <dt>INCOMPLETE</dt>
     *  <dd>Set the job state to INCOMPLETE.</dd>
     *  <dt>WAIT</dt>
     *  <dd>Wait until the job completes.</dd>
     * @oddjob.required No, Defaults to COMPLETE.
	 */
	private transient LoosingAction onLoosing;
	
	/** Track the child job. */
	private transient ChildHelper<Runnable> childHelper; 
		
	/**
     * @oddjob.property
     * @oddjob.description The keeper of work from which this job
     * attempts to grab work.
     * @oddjob.required Yes.
	 */
	private transient Keeper keeper;
	
	/**
     * @oddjob.property
     * @oddjob.description This job's identifier which is unique to
     * the Oddjob process, such as server name.
     * @oddjob.required Yes.
	 */
	private String identifier;
	
	/**
     * @oddjob.property
     * @oddjob.description The instance of identifier for a single grab. 
     * This is an identifier for each run of the grab jobb and will be
     * something like the scheduled date/time.
     * @oddjob.required Yes.
	 */
	private Object instance;
	
	/**
     * @oddjob.property
     * @oddjob.description The identifier of the winner. Will be equal
     * to this jobs identifier if this job has won.
     * @oddjob.required R/O.
	 */
	private String winner;

	/** Listens to either the child or the keeper to update
	 * this job's state. 	 */
	private transient GrabListener listener;

	/**
	 * Constructor.
	 */
	public GrabJob() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		childHelper = new ChildHelper<Runnable>(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public final void run() {
		
		OddjobNDC.push(loggerName());
		try {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					if (listener != null) {
						listener.stop();
					}
					getStateChanger().setJobState(JobState.EXECUTING);
				}					
			})) {
				return;
			}
			
			logger().info("[" + GrabJob.this + "] Executing.");

			try {
				configure();
				
				execute();
			}
			catch (final Throwable e) {
				logger().error("[" + GrabJob.this + "] Job Exception.", e);
				
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						getStateChanger().setJobStateException(e);
					}
				});
			}	
			logger().info("[" + GrabJob.this + "] Execution finished.");
		}
		finally {
			OddjobNDC.pop();
		}
	}
	
	/**
	 * Do the actual executing.
	 */
	private void execute() {
		
		Runnable childJob = childHelper.getChild();
		
		if (childJob == null) {
			throw new NullPointerException("No child job.");
		}
		
		Stateful statefulChild = (Stateful) childJob;
		
		if (keeper == null) {
			throw new NullPointerException("No Keeper.");
		}

		final Outcome outcome = keeper.grab(identifier, instance);
		
		winner = outcome.getWinner();
		
		if (outcome.isWon()) {
			listener = new ChildWatcher(statefulChild, 
					(WinningOutcome) outcome);				
			childJob.run();
		}
		else {
			LoosingAction loosingAction = this.onLoosing;
			if (loosingAction == null) {
				loosingAction = LoosingAction.COMPLETE;
			}
			switch (loosingAction) {
			case COMPLETE:
				stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
					public void run() {
						getStateChanger().setJobState(JobState.COMPLETE);
					}
				});
				break;
			case INCOMPLETE:
				stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
					public void run() {
						getStateChanger().setJobState(JobState.INCOMPLETE);
					}
				});
				break;
			case WAIT:
				listener = new StandBackAndWatch((LoosingOutcome) outcome);
				break;
			default: 
				throw new IllegalStateException("Unexpected Action!");
			}
		}
	}
	
	/**
	 * Watch the keeper.
	 */
	class StandBackAndWatch implements JobStateListener, GrabListener {

		private final LoosingOutcome outcome;
				
		StandBackAndWatch(LoosingOutcome outcome) {
			this.outcome = outcome;
			outcome.addJobStateListener(this);
		}

		@Override
		public void jobStateChange(JobStateEvent event) {
			final JobState state = event.getJobState();
			
			switch (state) {
			case COMPLETE:
			case INCOMPLETE:
			case EXCEPTION:
				outcome.removeJobStateListener(this);
				listener = null;			
				stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
					public void run() {
						getStateChanger().setJobState(state);
					}
				});
			}
		}
		
		synchronized public void stop() {
			stopListening();
			stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
					getStateChanger().setJobState(JobState.INCOMPLETE);
				}
			});
		}
		
		@Override
		public void stopListening() {
			outcome.removeJobStateListener(this);
			listener = null;
		}
	}
	
	interface GrabListener {

		public void stop();
		
		public void stopListening();
	}
	
	/**
	 * Watch the child.
	 *
	 */
	class ChildWatcher extends AbstractJobStateListener
	implements GrabListener {

		private final StateChanger stateChanger = 
			new OrderedStateChanger(getStateChanger(), stateHandler);
		
		private final Stateful child; 
		
		private final WinningOutcome outcome;

		ChildWatcher(Stateful child, WinningOutcome outcome) {
			this.child = child;
			this.outcome = outcome;
			child.addJobStateListener(this);
		}

		@Override
		protected void jobStateReady(Stateful source, Date time) {
			stateChanger.setJobState(JobState.READY, time);
			checkStop();
		}
		
		@Override
		protected void jobStateExecuting(Stateful source, Date time) {
			stateChanger.setJobState(JobState.EXECUTING, time);
		}
		
		@Override
		protected void jobStateComplete(Stateful source, Date time) {
			stateChanger.setJobState(JobState.COMPLETE, time);
			outcome.complete();
			checkStop();
		}
		
		@Override
		protected void jobStateNotComplete(Stateful source, Date time) {
			stateChanger.setJobState(JobState.INCOMPLETE, time);
			checkStop();
		}
	
		@Override
		protected void jobStateException(Stateful source, Date time,
				Throwable throwable) {
			stateChanger.setJobStateException(throwable, time);
			checkStop();
		}
		
		private void checkStop() {
			if (stop) {
				stopListening();
			}
		}

		@Override
		public void stopListening() {
			child.removeJobStateListener(this);
			listener = null;
		}
		
		synchronized public void stop() {
			if (child instanceof GrabListener) {
				((GrabListener) child).stop();
			}
		}
	}

	@Override
	public void stop() throws FailedToStopException {
		stateHandler.assertAlive();

		logger().debug("[" + this + "] Thread [" + 
				Thread.currentThread().getName() + "] requested  stop, " +
						"state is [" + lastJobStateEvent().getJobState() + "]");
		
		if (!stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
			public void run() {
				stop = true;
			}
		})) {
			return;			
		}
		
		logger().info("[" + this + "] Stop requested.");
		
		iconHelper.changeIcon(IconHelper.STOPPING);
		
		try {
			if (listener != null) {
				listener.stop();					
				listener = null;
			}

			childHelper.stopChildren();
			
 			new StopWait(this).run();
		}
		catch (FailedToStopException e) {
			iconHelper.changeIcon(IconHelper.EXECUTING);
			throw e;
		}
		
		logger().info("[" + this + "] Stopped.");		
	}
	
	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
			public void run() {
			
				logger().debug("[" + GrabJob.this + "] Propergating Soft Reset to children.");			
				
				if (listener != null) {
					listener.stopListening();
				}
				
				childHelper.softResetChildren();
				reset();
				getStateChanger().setJobState(JobState.READY);
				
				logger().info("[" + GrabJob.this + "] Soft Reset.");
			}
		});	
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		
		return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
			public void run() {
				logger().debug("[" + GrabJob.this + "] Propergating Hard Reset to children.");			
				
				if (listener != null) {
					listener.stopListening();
				}
				
				childHelper.hardResetChildren();
				reset();
				getStateChanger().setJobState(JobState.READY);
				
				logger().info("[" + GrabJob.this + "] Hard Reset.");
			}
		});
	}

	private void reset() {
		stop = false;		
		winner = null;
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
	 * The child.
	 * 
	 * @oddjob.property job
	 * @oddjob.description The child job.
	 * @oddjob.required No, but pointless if missing.
	 * 
	 * @param job A child
	 */
	@ArooaComponent
	public void setJob(Runnable job) {
		if (job == null) {
			childHelper.removeChildAt(0);
		}
		else {
			childHelper.insertChild(0, job);
		}
	}	
	
	public void setKeeper(Keeper keeper) {
		this.keeper = keeper;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Object getInstance() {
		return instance;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}

	public String getWinner() {
		return winner;
	}
	
	public LoosingAction getOnLoosing() {
		return onLoosing;
	}

	public void setOnLoosing(LoosingAction loosingAction) {
		this.onLoosing = loosingAction;
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

}
