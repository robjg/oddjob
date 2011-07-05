package org.oddjob.scheduling;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.images.IconHelper;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;
import org.oddjob.util.Clock;
import org.oddjob.util.DefaultClock;

/**
 * Common functionality for Timers.
 * 
 * @author rob
 *
 */
abstract public class TimerBase extends ScheduleBase {

	private static final long serialVersionUID = 2009091400L; 
	
	/**
	 * @oddjob.property schedule
	 * @oddjob.description The Schedule used to provide execution 
	 * times.
	 * @oddjob.required Yes.
	 */
	private transient Schedule schedule;
		
	/** 
	 * @oddjob.property
	 * @oddjob.description The time zone the schedule is to run
	 * in. This is the text id of the time zone, such as "Europe/London".
	 * More information can be found at
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/TimeZone.html">
     * TimeZone</a>.
	 * @oddjob.required Set automatically.  
	 */
	private transient TimeZone timeZone;

	/**
	 * @oddjob.property 
	 * @oddjob.description The clock to use. Tells the current time.
	 * @oddjob.required Set automatically.
	 */ 
	private transient Clock clock;
	
	/** The currently scheduled job future. */
	private transient volatile Future<?> future;
	
	/** The scheduler to schedule on. */
	private transient ScheduledExecutorService scheduler;

	/** Provided to the schedule. */
	protected final Map<Object, Object> contextData = 
			Collections.synchronizedMap(new HashMap<Object, Object>());
	
	/** The next due date. */
	private volatile transient Date nextDue;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description This is the current/next interval from the
	 * schedule.
	 * @oddjob.required Set automatically.
	 */ 
	private volatile IntervalTo current;
	
	@ArooaHidden
	@Inject
	public void setScheduleExecutorService(ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}
	
	@Override
	protected void begin() throws Throwable {
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
			future = null;
		}	
	}
	
	protected void onReset() {
		contextData.clear();
	}
	

	/**
	 * Get the time zone id to use in this schedule.
	 * 
	 * @return The time zone idbeing used.
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
	 * @throws ComponentPersistException 
	 * @oddjob.property reschedule 
	 * @oddjob.description Reschedule from the given date/time.
	 * @oddjob.required Only available when running.
	 */ 
	@ArooaHidden
	public void setReschedule(Date reSchedule) throws ComponentPersistException {
		if (future != null) {
			future.cancel(true);
			future = null;
		}
		
		scheduleFrom(reSchedule);
	}
	
	protected void scheduleFrom(Date date) throws ComponentPersistException {
	    logger().debug("[" + this + "] Scheduling from [" + date + "]");

	    ScheduleContext context = new ScheduleContext(
	    		date, timeZone, contextData, getLimits());

	    current = schedule.nextDue(context);
	    if (current == null) {
	    	setNextDue(null);
	    }
	    else {
	    	setNextDue(current.getFromDate());
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
	 * @param nextDue The date schedule is next due.
	 * @throws ComponentPersistException 
	 */
	protected void setNextDue(Date nextDue) throws ComponentPersistException {
		
		logger().debug("[" + this + "] Setting next due to : " + nextDue);
		Date oldNextDue = this.nextDue;
		this.nextDue = nextDue;	
		firePropertyChange("nextDue", oldNextDue, nextDue);
		
		if (nextDue == null) {
			logger().info("[" + this + "] Schedule finished.");
			childStateReflector.start();
			return;
		}
		else {
			iconHelper.changeIcon(IconHelper.SLEEPING);
		}

		// save the last complete.
		save();
		
		long delay = nextDue.getTime() - getClock().getDate().getTime();
		if (delay < 0) {
			delay = 0;
		}
		
	    future = scheduler.schedule(
	    		new Execution(), delay, TimeUnit.MILLISECONDS);
	    
		logger().info("[" + this + "] scheduled for execution at " + 
				nextDue + " in " + delay + "ms");
	}

	/**
	 * Get the current/next interval.
	 * 
	 * @return The interval, null if not due again. 
	 */
	public IntervalTo getCurrent() {
		return current;
	}

	/**
	 * @oddjob.property job
	 * @oddjob.description The job to run when it's due.
	 * @oddjob.required Yes.
	 */
	@ArooaComponent
	public synchronized void setJob(Runnable job) {
		if (job == null) {
			childHelper.removeChildAt(0);
		}
		else {
			childHelper.insertChild(0, job);
		}
	}
	
	
	abstract protected IntervalTo getLimits();
	
	abstract protected void rescheduleOn(JobState state) 
	throws ComponentPersistException;

	abstract protected void reset(Resetable job);
	
	/**
	 * Listen for changed child job states. Not these could come in on
	 * a different thread to that which launched the Executor.
	 *
	 */
	class RescheduleStateListener implements JobStateListener {
		
		public void jobStateChange(JobStateEvent event) {
			final JobState state = event.getJobState();
			
			if (stop) {
			    event.getSource().removeJobStateListener(this);
				return;
			}
			
			if (state == JobState.READY) {
				return;
			}
						
			if (state == JobState.EXECUTING) {
				iconHelper.changeIcon(IconHelper.EXECUTING);
				return;
			}

			// Order is important! Must remove this before scheduling again.
		    event.getSource().removeJobStateListener(this);
		    
			logger().debug("[" + TimerBase.this + "] Rescheduling based on state [" + state + "]");
			
			try {
				rescheduleOn(state);
			} catch (final ComponentPersistException e) {
				stateHandler.waitToWhen(new IsAnyState(), 
						new Runnable() {
							@Override
							public void run() {
								getStateChanger().setJobStateException(e);
							}
						});
			}
			
		}
	}
	
	/**
	 */
	class Execution implements Runnable {
		
		public void run() {
			
			if (stop) {
				logger().info("[" + TimerBase.this + 
		    		"] Not Executing child as we have now stopped.");
				return;
			}
			
		    logger().info("[" + TimerBase.this + 
		    		"] Executing child at [" + new Date()+ "]");
		    
		    Runnable job = childHelper.getChild();
		    
		    if (job != null) {
		    
				try {
				    if (job instanceof Resetable) {
				    	reset((Resetable) job);
			        }
				    
			    	if (job instanceof Stateful) {
			    		
			    		((Stateful) job).addJobStateListener(
			    				new RescheduleStateListener());
			    	}

				    logger().debug("Running job [" + job + "]");
				    
					job.run();
				}
				catch (final Exception t) {
					logger().error("Failed running scheduled job.", t);
					stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
						public void run() {
							getStateChanger().setJobStateException(t);
						}
					});
				}
		    }
		    else {
			    logger().warn("Nothing to run.");
		    }
		}
		
		@Override
		public String toString() {
			return TimerBase.this.toString();
		}
	}
		
}
