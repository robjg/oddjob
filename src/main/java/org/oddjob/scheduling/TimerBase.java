package org.oddjob.scheduling;

import org.oddjob.FailedToStopException;
import org.oddjob.Resettable;
import org.oddjob.Stateful;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.jobs.job.ResetAction;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.scheduling.state.TimerState;
import org.oddjob.state.*;
import org.oddjob.util.Clock;
import org.oddjob.util.DefaultClock;
import org.oddjob.util.OddjobLockedException;
import org.oddjob.util.Restore;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Common functionality for Timers.
 * 
 * @author rob
 *
 */
abstract public class TimerBase extends ScheduleBase {

	private static final long serialVersionUID = 2009091420120126L; 
	
	/**
	 * @oddjob.property schedule
	 * @oddjob.description The Schedule used to provide execution 
	 * times.
	 * @oddjob.required Yes.
	 */
	private transient volatile Schedule schedule;
		
	/** 
	 * @oddjob.property
	 * @oddjob.description The time zone the schedule is to run
	 * in. This is the text id of the time zone, such as "Europe/London".
	 * More information can be found at
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/TimeZone.html">
     * TimeZone</a>.
	 * @oddjob.required Set automatically.  
	 */
	private transient volatile TimeZone timeZone;

	/**
	 * @oddjob.property 
	 * @oddjob.description The clock to use. Tells the current time.
	 * @oddjob.required Set automatically.
	 */ 
	private transient volatile Clock clock;
	
	/** The currently scheduled job future. */
	private transient volatile Future<?> future;
	
	/** The scheduler to schedule on. */
	private transient volatile ScheduledExecutorService scheduler;

	/** Provided to the schedule. */
	protected final Map<Object, Object> contextData = 
			Collections.synchronizedMap(new HashMap<>());
	
	/**
	 * @oddjob.property 
	 * @oddjob.description The time the next execution is due. This property
	 * is updated when the timer starts or after each execution.
	 * @oddjob.required Read Only.
	 */
	private transient volatile Date nextDue;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description This is the current/next result from the
	 * schedule. This properties fromDate is used to set the nextDue date for
	 * the schedule and it's useNext (normally the same as toDate) property is 
	 * used to calculate the following new current property after execution. This
	 * property is most useful for the Timer to pass limits to 
	 * the Retry, but is also useful for diagnostics.
	 * @oddjob.required Set automatically.
	 */ 
	private volatile ScheduleResult current;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description The time the schedule was lastDue. This is set
	 * from the nextDue property when the job begins to execute.
	 * @oddjob.required Read only.
	 */ 
	private volatile Date lastDue;

	/**
	 * @oddjob.property 
	 * @oddjob.description The state of the Child Job
	 * from the nextDue property when the job begins to execute.
	 * @oddjob.required Read only.
	 */ 
	private transient volatile StateCondition haltOn;
	
	private transient volatile ResetAction reset;
	
	@ArooaHidden
	@Inject
	public void setScheduleExecutorService(ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}
	
	@Override
	protected void begin() throws ComponentPersistException {
		if (schedule == null) {
			throw new NullPointerException("No Schedule.");
		}
		
		if (scheduler == null) {
			throw new NullPointerException("No Scheduler.");
		}
		
		if (clock == null) {
			clock = new DefaultClock();
		}
	}
	
	protected void onStop() {
		super.onStop();
		
		Future<?> future = this.future;
		if (future != null) {
			future.cancel(false);
			this.future = null;
		}	
	}
	
	@Override
	protected void postStop() {
		stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				getStateChanger().setState(TimerState.STARTABLE);
			}
		});
	}
	
	protected void onReset() {
		contextData.clear();
		nextDue = null;
		current = null;
		lastDue = null;
	}
	

	/**
	 * Get the time zone id to use in this schedule.
	 * 
	 * @return The time zone id being used.
	 */
	public String getTimeZone() {
		if (timeZone == null) {
			return null;
		}
		return timeZone.getID();
	}

	/**
	 * Set the time zone.
	 * 
	 * @param timeZoneId the timeZoneId.
 	 */
	public void setTimeZone(String timeZoneId) {
		if (timeZoneId == null) {
			this.timeZone = null; 
		} else {
			this.timeZone = TimeZone.getTimeZone(timeZoneId);
		}
	}
		
	/**
	 * Set the schedule.
	 * 
	 * @param schedule The schedule.
	 */
	public void setSchedule(Schedule schedule) {
	    this.schedule = schedule;
	}

	public Schedule getSchedule() {
		return schedule;
	}
	
	/**
	 * @throws OddjobLockedException
	 * 
	 * @oddjob.property reschedule 
	 * @oddjob.description Reschedule from the given date/time.
	 * @oddjob.required Only available once the timer has started.
	 */ 
	@ArooaHidden
	public void setReschedule(final Date reSchedule) throws OddjobLockedException {
		
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			if (!stateHandler().tryToWhen(new StateMatch(TimerState.STARTED), 
				new Runnable() {
					@Override
					public void run() {
						logger().info("Rescheduling with " + reSchedule); 
						
						try {
							CancelAndStopChild();
							scheduleFrom(reSchedule);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				})) {
				logger().info("Can only reschedule once the timer has started.");
			}
		}	
	}
	
	/**
	 * Cancel and child jobs that are submitted and stop any that are 
	 * running. This should only be run while locked because it
	 * temporarily sets the stop flag to fool the state listener into
	 * not rescheduling.
	 * 
	 * @throws FailedToStopException
	 */
	protected void CancelAndStopChild() throws FailedToStopException {
		
		Future<?> future = this.future;
		if (future != null) {
			future.cancel(false);
			this.future = null;
		}
		
		stop = true;
		childHelper.stopChildren();
		stop = false;
	}
	
	/**
	 * Schedule a job from a given date.
	 * 
	 * @param date The date to schedule the job from.
	 * 
	 * @return true if the job is scheduled again, false if it is not to
	 * be scheduled again.
	 * 
	 * @throws ComponentPersistException
	 */
	protected boolean scheduleFrom(Date date) throws ComponentPersistException {
	    logger().debug("Scheduling from [" + date + "]");

	    if (date == null) {
	    	return internalSetNextDue(null);
	    }
	    else {
		    ScheduleContext context = new ScheduleContext(
		    		date, timeZone, contextData, getLimits());
	
		    current = schedule.nextDue(context);
		    if (current == null) {
		    	return internalSetNextDue(null);
		    }
		    else {
		    	return internalSetNextDue(current.getFromDate());
		    }
	    }
	}

	/**
	 * Get the current clock.
	 * 
	 * @return The clock
	 */
	public Clock getClock() {
		if (clock == null) {
			clock = new DefaultClock();
		}
		return clock;
	}

	/**
	 * Set the clock. Only useful for testing.
	 * 
	 * @param clock The clock.
	 */
	public void setClock(Clock clock) {
		this.clock = clock;
	}
	
	/**
	 * Manually set the Next Due Date.
	 * 
	 * @param nextDue The Next Due Date. May be null.
	 * 
	 * @throws OddjobLockedException
	 */
	@ArooaHidden
	public void setNextDue(final Date nextDue) throws OddjobLockedException {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			if (!stateHandler().tryToWhen(new StateMatch(TimerState.STARTED),
				new Runnable() {
					@Override
					public void run() {
						logger().info("Manually setting nextDue to " + 
								nextDue); 
						try {
							CancelAndStopChild();
							internalSetNextDue(nextDue);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				})) {
				logger().info("Can't set nextDue until timer has STARTED."); 
			}
		}	
	}
	
	/**
	 * Get the next due date.
	 * 
	 * @return The next due date
	 */
	public Date getNextDue() {		
		return nextDue;
	}

	/**
	 * Set the next due date.
	 * 
	 * @param nextDue The date schedule is next due. If null the job won't 
	 * be scheduled and the child state reflector will be called to reflect
	 * the state of the child job.
	 * 
	 * @return true if the job was scheduled. False it wasn't. 
	 * 
	 * @throws ComponentPersistException 
	 */
	protected boolean internalSetNextDue(Date nextDue) throws ComponentPersistException {
		
		Date oldNextDue = this.nextDue;
		this.nextDue = nextDue;	
		firePropertyChange("nextDue", oldNextDue, nextDue);
		
		if (nextDue == null) {
			logger().info("There is no nextDue, schedule finished.");
			childStateReflector.start();
			return false;
		}
		
		// save the last complete.
		save();
		
		long delay = nextDue.getTime() - getClock().getDate().getTime();
		if (delay < 0) {
			delay = 0;
		}
		
		logger().info("Next due at " + nextDue +
				" in " + DateHelper.formatMilliseconds(delay) + ".");
		
		if (delay == 0) {
			changeStateLocked(TimerState.ACTIVE);
		}
		else {
			changeStateLocked(TimerState.STARTED);
		}
		
	    future = scheduler.schedule(
	    		new Execution(), delay, TimeUnit.MILLISECONDS);
	    
		return true;
	}

	/**
	 * Get the current/next interval.
	 * 
	 * @return The interval, null if not due again. 
	 */
	public ScheduleResult getCurrent() {
		return current;
	}

	/**
	 * Get the last due date.
	 * 
	 * @return The last due date, null when a timer starts for the first time.
	 */
	public Date getLastDue() {
	    return lastDue;
	}
	
	/**
	 * @oddjob.property job
	 * @oddjob.description The job to run when it's due.
	 * @oddjob.required Yes.
	 */
	@ArooaComponent
	public void setJob(Runnable job) {
		
		if (job == null) {
			childHelper.removeChildAt(0);
		}
		else {
			if (! (job instanceof Stateful)) {
				throw new IllegalStateException("Child job must be Stateful. [" +
						job + "] is of class " + job.getClass().getName());
			}
			childHelper.insertChild(0, job);
		}
	}
	
	/**
	 * Implementation provided by sub classes so limits are available in
	 * {@link #scheduleFrom(Date)}.
	 * 
	 * @return The limits, or null. Retry has limits, timer doesn't.
	 */
	abstract protected Interval getLimits();
	
	/**
	 * Reschedule a job.
	 * 
	 * @return True if it was schedule, false if it wasn't.
	 * 
	 * @throws ComponentPersistException
	 */
	protected boolean reschedule() 
	throws ComponentPersistException {
		
    	Date use = getCurrent().getUseNext();
    	Date now = getClock().getDate();
    	if (use != null &&  isSkipMissedRuns() && use.before(now)) {
    		use = now;
    	}
    	
    	return scheduleFrom(use);
	}

	/**
	 * Implementation provided by sub classes to decide what kind of reset to send
	 * to the child. Timer sends a hard reset, Retry sends a soft reset.
	 * 
	 * @param job The child job that will be reset.
	 */
	protected void reset(Resettable job) {
	
		ResetAction resetAction = this.reset;
		if (reset == null) {
			resetAction = getDefaultReset();
		}
		
	    logger().debug("Sending [" + resetAction + "] Reset to [" + job + "]");
	    
		resetAction.doWith(job);
	}
	
	/**
	 * Listen for changed child job states. Note these could come in on
	 * a different thread to that which launched the Executor.
	 *
	 */
	class RescheduleStateListener implements StateListener {
		
		@Override
		public void jobStateChange(StateEvent event) {
			
			
			try (Restore restore = ComponentBoundary.push(loggerName(), TimerBase.this)) {
				handleChildState(event, this);
			} catch (final ComponentPersistException e) {
				stateHandler().waitToWhen(new IsAnyState(), 
					new Runnable() {
						@Override
						public void run() {
							getStateChanger().setStateException(e);
						}
					});
			}
		}
	}
	
	protected void handleChildState(StateEvent event, StateListener listener) 
	throws ComponentPersistException {
		
		State state = event.getState();
		
		if (stop || state.isDestroyed()) {
		    event.getSource().removeStateListener(listener);
			return;
		}
		
		if (state.isReady()) {
			return;
		}
		
		StateCondition haltOn = getHaltOn();
		if (haltOn == null) {
			haltOn = getDefaultHaltOn();
		}
		
		if (haltOn.test(state)) {
		    event.getSource().removeStateListener(listener);
	    	internalSetNextDue(null);
			return;
		}
		
		if (state.isStoppable()) {
			if (state.isComplete()) {
				changeStateLocked(TimerState.STARTED);
			}
			else {
				changeStateLocked(TimerState.ACTIVE);
			}
			return;
		}
		
		// Order is important! Must remove this before scheduling again.
	    event.getSource().removeStateListener(listener);
	    
		reschedule();
	}
	
	protected boolean changeStateLocked(final TimerState required) {
		return stateHandler().waitToWhen(
				new IsNot(new StateMatch(required)), new Runnable() {
			@Override
			public void run() {
				getStateChanger().setState(required);
			}
		});
		
	}
	
	/**
	 */
	class Execution implements Runnable {
		
		public void run() {
			
			try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
				try {
					// Wait for the timer to start to ensure predictable
					// state transitions.
					begun.await();
				} catch (InterruptedException e) {
					logger().warn("Interrupted.");
					Thread.currentThread().interrupt();
					return;
				}
				
				Runnable job = childHelper.getChild();

				if (stop) {
					logger().info("Not Executing [" + job + "] + as we have now stopped.");
					return;
				}

				if (job == null) {
					logger().warn("Nothing to run. Job is null!");
					return;
				}
				
				logger().info("Executing [" + job + "] due at " + nextDue);

				lastDue = nextDue;
				
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						getStateChanger().setState(TimerState.ACTIVE);
					}
				});
				
				try {
					reset((Resettable) job);

					((Stateful) job).addStateListener(
							new RescheduleStateListener());
					
					job.run();

					logger().info("Finished executing [" + 
							job + "]");
					
				}
				catch (final Exception t) {
					logger().error("Failed running scheduled job.", t);
					stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
						public void run() {
							getStateChanger().setStateException(t);
						}
					});
				}
			}
		}
		
		@Override
		public String toString() {
			return TimerBase.this.toString();
		}
	}

	public StateCondition getHaltOn() {
		return haltOn;
	}

	@ArooaAttribute
	public void setHaltOn(StateCondition haltOn) {
		this.haltOn = haltOn;
	}

	abstract protected StateCondition getDefaultHaltOn();
	
	abstract protected boolean isSkipMissedRuns();

	public ResetAction getReset() {
		return reset;
	}

	@ArooaAttribute
	public void setReset(ResetAction reset) {
		this.reset = reset;
	}
	
	abstract protected ResetAction getDefaultReset();
}
