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
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.framework.extend.BasePrimary;
import org.oddjob.framework.util.ComponentBoundry;
import org.oddjob.framework.util.StopWait;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.persist.Persistable;
import org.oddjob.scheduling.Keeper;
import org.oddjob.scheduling.LoosingOutcome;
import org.oddjob.scheduling.Outcome;
import org.oddjob.scheduling.WinningOutcome;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateChanger;
import org.oddjob.state.JobStateConverter;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.OrderedStateChanger;
import org.oddjob.state.State;
import org.oddjob.state.StateChanger;
import org.oddjob.state.StateCondition;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;
import org.oddjob.util.Restore;

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

	/** Handle state. */
	private transient volatile JobStateHandler stateHandler;
	
	/** Used to notify clients of an icon change. */
	private transient volatile IconHelper iconHelper;
	
	/** Used for state changes. */
	private transient volatile JobStateChanger stateChanger;

	/** stop flag. */
	protected transient volatile boolean stop;
	
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
		stateHandler = new JobStateHandler(this);
		childHelper = new ChildHelper<Runnable>(this);
		iconHelper = new IconHelper(this, 
				StateIcons.iconFor(stateHandler.getState()));
		stateChanger = new JobStateChanger(stateHandler, iconHelper, 
				new Persistable() {					
			@Override
			public void persist() throws ComponentPersistException {
				save();
			}
		});
	}

	@Override
	protected JobStateHandler stateHandler() {
		return stateHandler;
	}
	
	@Override
	protected IconHelper iconHelper() {
		return iconHelper;
	}
	
	protected StateChanger<JobState> getStateChanger() {
		return stateChanger;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public final void run() {
		
		try (Restore restore = ComponentBoundry.push(loggerName(), this)) {		
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					if (listener != null) {
						listener.stop();
					}
					getStateChanger().setState(JobState.EXECUTING);
				}					
			})) {
				return;
			}
			
			logger().info("Executing.");

			try {
				configure();
				
				execute();
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
						getStateChanger().setState(JobState.COMPLETE);
					}
				});
				break;
			case INCOMPLETE:
				stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
					public void run() {
						getStateChanger().setState(JobState.INCOMPLETE);
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
	class StandBackAndWatch implements StateListener, GrabListener {

		private final LoosingOutcome outcome;
				
		StandBackAndWatch(LoosingOutcome outcome) {
			this.outcome = outcome;
			outcome.addStateListener(this);
		}

		@Override
		public void jobStateChange(StateEvent event) {
			final State state = event.getState();

			// Should we use ENDED?
			StateCondition finishedCondition = StateConditions.FINISHED;
			if (finishedCondition.test(state)) {
				outcome.removeStateListener(this);
				listener = null;			
				stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
					public void run() {
						getStateChanger().setState(
								new JobStateConverter().toJobState(state));
					}
				});
			}
		}
		
		synchronized public void stop() {
			stopListening();
			stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
					getStateChanger().setState(JobState.INCOMPLETE);
				}
			});
		}
		
		@Override
		public void stopListening() {
			outcome.removeStateListener(this);
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
	class ChildWatcher
	implements GrabListener, StateListener {

		private final StateChanger<JobState> stateChanger = 
			new OrderedStateChanger<JobState>(getStateChanger(), stateHandler);
		
		private final Stateful child; 
		
		private final WinningOutcome outcome;

		ChildWatcher(Stateful child, WinningOutcome outcome) {
			this.child = child;
			this.outcome = outcome;
			child.addStateListener(this);
		}

		@Override
		public void jobStateChange(StateEvent event) {
			State state = event.getState();
			Date time = event.getTime();
			
			if (state.isReady()) {
				stateChanger.setState(JobState.READY, time);
				checkStop();
			}
			else if (state.isStoppable()) {
				stateChanger.setState(JobState.EXECUTING, time);
			}
			else if (state.isComplete()) {
				stateChanger.setState(JobState.COMPLETE, time);
				outcome.complete();
				checkStop();
			}
			else if (state.isIncomplete()) {
				stateChanger.setState(JobState.INCOMPLETE, time);
				checkStop();
			}
			else if (state.isException()) {
				stateChanger.setStateException(event.getException(), time);
				checkStop();
			}
			else {
				stateChanger.setStateException(new JobDestroyedException(child), time);
				checkStop();
			}
		}
	
		/**
		 * shared check to see if listener should remove itself.
		 */
		private void checkStop() {
			if (stop) {
				stopListening();
			}
		}

		@Override
		public void stopListening() {
			child.removeStateListener(this);
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

		try (Restore restore = ComponentBoundry.push(loggerName(), this)) {		
			logger().debug("Stop requested.");

			if (!stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
					stop = true;
				}
			})) {
				logger().debug("Not in a stoppable state.");
				return;			
			}

			logger().info("Stopping.");

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

			logger().info("Stopped.");		
		}
	}
	
	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		try (Restore restore = ComponentBoundry.push(loggerName(), this)) {		
			return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {

					logger().debug("Propagating Soft Reset to children.");			

					if (listener != null) {
						listener.stopListening();
					}

					childHelper.softResetChildren();
					reset();
					getStateChanger().setState(JobState.READY);
					stop = false;

					logger().info("Soft reset complete.");
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

					if (listener != null) {
						listener.stopListening();
					}

					childHelper.hardResetChildren();
					reset();
					getStateChanger().setState(JobState.READY);
					stop = false;

					logger().info("Hard reset complete.");
				}
			});
		}
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

	/**
	 * Internal method to fire state.
	 */
	protected void fireDestroyedState() {
		
		if (!stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				stateHandler().setState(JobState.DESTROYED);
				stateHandler().fireEvent();
			}
		})) {
			throw new IllegalStateException("[" + GrabJob.this + "] Failed set state DESTROYED");
		}
		logger().debug("[" + this + "] Destroyed.");				
	}
}
