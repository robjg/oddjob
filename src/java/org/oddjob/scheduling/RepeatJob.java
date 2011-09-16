/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.scheduling;

import java.util.Date;

import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.StructuralJob;
import org.oddjob.images.IconHelper;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleCalculator;
import org.oddjob.schedules.ScheduleListener;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.State;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.state.StateOperator;
import org.oddjob.state.WorstStateOp;
import org.oddjob.util.Clock;
import org.oddjob.util.DefaultClock;

/**
 * @oddjob.description This job will repeatedly run it's
 * child job. The repeats are determined by schedules. An optional
 * regular schedule and an optional retry schedule can be configured.
 * <p>
 * A repeat without a schedule will execute once.  
 * <p>
 * An exception job can be configured which will be run if the
 * child either doesn't complete or ends in an exception state. If 
 * a retry schedule is specified
 * then the exception job won't be triggered until the retry schedule
 * expires.
 * <p>
 * This job is deprecated. Prefer {@link Timer} and {@link Retry} instead
 * as they don't cause a thread to sleep that might be doing useful work.
 * This job may be re-written in a non time based form. Possibly as
 * repeat until a job state or repeat for a count. The use cases still
 * need to be discovered.
 * 
 * @oddjob.example
 * 
 * Repeat 10 times.
 * 
 * <code><pre>
 * &lt;repeat xmlns:schedules="http://rgordon.co.uk/oddjob/schedules&gt;
 *   &lt;schedule&gt;
 *      &lt;schedules:count count="10"/&gt;
 *   &lt;/schedule&gt;
 *   &lt;job&gt;
 *     &lt;sequential&gt;
 *       &lt;sequence id="seq"/&gt;
 *       &lt;echo text="Hello ${seq.current}"/&gt;
 *     &lt;/sequential&gt;
 *   &lt;/job&gt;
 * &lt;/repeat&gt;
 * </pre></code>
 * 
 * @oddjob.example
 * 
 * Repeat immediately until complete.
 * 
 * <code><pre>
 * &lt;repeat xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"&gt;
 *   &lt;retry&gt;
 *      &lt;schedules:now/&gt;
 *   &lt;/retry&gt;
 *   &lt;job&gt;
 *        &lt;random class="org.oddjob.samples.RandomTask"/&gt;
 *   &lt;/job&gt;
 * &lt;/repeat&gt;
 * </pre></code>
 * 
 * @oddjob.example 
 * 
 * Try something 10 times, 1 second apart before alerting.
 * 
 * <code><pre>
 * &lt;repeat xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"&gt;
 *   &lt;retry&gt;
 *      &lt;schedules:count count="10"&gt;
 *      	&lt;schedules:interval interval="00:00:01"/&gt;
 *      &lt;/schedules:count&gt;
 *   &lt;/retry&gt;
 *   &lt;child&gt;
 *     &lt;oddjob file="doesnt.exist"/&gt;
 *   &lt;/child&gt;
 *   &lt;exception&gt;
 *     &lt;echo text="Help!"/&gt; 
 *   &lt;/exception&gt;
 * &lt;/repeat&gt;
 * </pre></code>
 * 
 * @author Rob Gordon.
 * 
 * @deprecated Prefer {@link Timer} and {@link Retry} instead.
 */
public class RepeatJob extends StructuralJob<Runnable>
implements Stoppable {
	private static final long serialVersionUID = 20051121;
	
    /**
     * @oddjob.property schedule
     * @oddjob.description A schedule for normal completion. It defaults 
     * to immediately.
     * @oddjob.required No.
     */
    private transient Schedule normalSchedule;
    
	/**
	 * @oddjob.property retry
	 * @oddjob.description The schedule used in the event 
	 * that the child job doesn't complete or is in an
	 * exception state.
	 * @oddjob.required No.
	 */
	private transient Schedule retrySchedule;

    private transient Runnable exceptionJob;

    /** The clock - now only used for testing.
	 */
	private transient Clock clock;

	/** Next due date */
	private transient Date nextDue;

	/** Retry, so only soft reset. */
	private transient boolean retry;
	
	/** failed */
	private transient boolean failed;

	/**
	 * @oddjob.property 
	 * @oddjob.description This is the start of the last normal schedule. 
	 * The schedule date is used when the retry schedule polls past midnight,
	 * yet your business data is from the day before. The schedule date provides
	 * a consistent business date which can useful as the date for 
	 * file names etc.
	 * @oddjob.required No.
	 */ 
	private transient Date scheduleDate;
	
	@Override
	protected StateOperator getStateOp() {
		return new WorstStateOp();
	}
	
	/** 
	 * @oddjob.property job
	 * @oddjob.description The job who's execution 
	 * to schedule. 
	 * @oddjob.required Yes.
	 */
	@ArooaComponent
	public void setJob(Runnable child) {
		if (child == null) {
			childHelper.removeChildAt(0);
		}
		else {
			if (!(child instanceof Stateful)) {
				throw new IllegalArgumentException("Child must be Stateful.");
			}

			if (childHelper.size() > 0) {
				throw new IllegalArgumentException("Child Job already set.");
			}
			childHelper.insertChild(0, child);
		}
	}
	
    /**
     * @oddjob.property exception
     * @oddjob.description Add a job to execute in
     * the event that the child job has not completed or
     * is in an exception state and the retry schedule has
     * expired or there was no retry schedule.
     * @oddjob.required No.
     * 
     * @param exceptionJob The exception job.
     */
	@ArooaAttribute
    public void setException(Runnable exceptionJob) {
        this.exceptionJob = exceptionJob;
    }
    
	/**
	 * Set the schedule.
	 * 
	 * @param schedule The schedule.
	 */
	public void setSchedule(Schedule schedule) {
	    this.normalSchedule = schedule;
	}
	
	/**
	 * Getter for schedule.
	 * 
	 * @return The schedule.
	 */
	public Schedule getSchedule() {
	    return this.normalSchedule;
	}
		
	/**
	 * Set the retry schedule.
	 * 
	 * @param retry
	 */
	public void setRetry(Schedule retry) {
	    this.retrySchedule = retry;
	}
	
	/**
	 * Getter for retry.
	 * 
	 * @return The retry schedule.
	 */
	public Schedule getRetry() {
	    return this.retrySchedule;
	}
	
	/**
	 * Get the clock to use in this schedule.
	 * 
	 * @return The clock being used.
	 */
	Clock getClock() {
		if (clock == null) {
			clock = new DefaultClock();
		    logger().debug("Using default clock.");
		}
		return clock;
	}

	/**
	 * Set the clock. Only useful during testing.
	 * 
	 * @param clock The clock.
 	 */
	void setClock(Clock clock) {
	    this.clock = clock;
	    logger().debug("set clock [" + clock + "]");
	}

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jobs.AbstractJob#execute()
     */
	protected void execute() {
		Object[] children = childHelper.getChildren(); 
		if (children.length == 0) {
			return;
		}

		// initialise a new schedule calculator
		ScheduleCalculator scheduleCalculator = new ScheduleCalculator(getClock(), 
				normalSchedule, retrySchedule, null);
		scheduleCalculator.addScheduleListener(new ScheduleCalculateListener());
		scheduleCalculator.initialise();
		
		while (!stop) {
			Date timeNow = getClock().getDate();
		    logger().debug("Time now is " + timeNow);
		    
		    if (failed) {
		        if (exceptionJob != null) {
		        	if (exceptionJob instanceof Resetable) {
		        		((Resetable) exceptionJob).hardReset();
		        	}
		        	exceptionJob.run();
		        }
		    	failed = false;
		    }
		    else {
		    	if (nextDue == null) {
				    logger().debug("Schedule finished.");
				    break;
		    	}
		    	
		    	long sleepTime = nextDue.getTime() - timeNow.getTime();

		    	if (sleepTime <= 0) {
			    // time to run
				    logger().debug("Executing at [" + timeNow + "]");
				    
				    runJob((Runnable) children[0],
				    		scheduleCalculator);
			    }
		    	else {
		    		logger().debug(getName() + " next due at "
		    				+ getNextDue());
					sleep(sleepTime);
		    	} 
			} // if retry
		} // end while
		
	}

	/**
	 * Utility method to sleep a certain time.
	 * 
	 * @param waitTime Milliseconds to sleep for.
	 */
	protected void sleep(final long waitTime) {
		stateHandler().assertAlive();
		
		if (!stateHandler().waitToWhen(new IsStoppable(), new Runnable() {
			public void run() {
				if (stop) {
					logger().debug("[" + RepeatJob.this + 
					"] Stop request detected. Not sleeping.");
					
					return;
				}
				
				logger().debug("[" + RepeatJob.this + "] Sleeping for " + ( 
						waitTime == 0 ? "ever" : "[" + waitTime + "] milli seconds") + ".");
				
				iconHelper.changeIcon(IconHelper.SLEEPING);
					
				try {
					stateHandler().sleep(waitTime);
				} catch (InterruptedException e) {
					logger().debug("Sleep interupted.");
				}
				
				iconHelper.changeIcon(IconHelper.EXECUTING);
			}
		})) {
			throw new IllegalStateException("Can't sleep unless EXECUTING.");
		}
	}	
		
	private void runJob(Runnable job, ScheduleCalculator scheduleCalculator) {
		
        if (job instanceof Resetable) {
		    if (retry) {
				((Resetable) job).softReset();
			}
			else {
				((Resetable) job).hardReset();
			}
        }
        
		ScheduleStateListener ssl = new ScheduleStateListener();
		((Stateful) job).addStateListener(ssl);

		try {
			job.run();
		}
		finally {
			((Stateful) job).removeStateListener(ssl);
		}
		
		if (ssl.state.isException()) {
			logger().debug("Job [" + job + "] Exception");
			scheduleCalculator.calculateRetry();			
		}
		else if (ssl.state.isIncomplete()) {
			logger().debug("Job [" + job + "] Not Complete.");
			scheduleCalculator.calculateRetry();
		}
		else if (ssl.state.isComplete()) {
			logger().debug("Job [" + job + "] Complete");
			scheduleCalculator.calculateComplete();
		}
		else {
			logger().debug("Job state for [" + job + 
					"] is: " + ssl.state + ", Will not repeat.");
			nextDue = null;
		}
	}
	
	class ScheduleCalculateListener implements ScheduleListener {

		/* (non-Javadoc)
		 * @see org.oddjob.treesched.ScheduleListener#initialised(java.util.Date)
		 */
		public void initialised(Date scheduleDate) {
			RepeatJob.this.scheduleDate = scheduleDate;
			RepeatJob.this.nextDue = scheduleDate;
			RepeatJob.this.retry = false;
			RepeatJob.this.failed = false;
		}
		
		/* (non-Javadoc)
		 * @see org.oddjob.treesched.ScheduleListener#complete(java.util.Date, java.util.Date)
		 */
		@Override
		public void complete(Date scheduleDate, Interval lastComplete) {
			RepeatJob.this.scheduleDate = scheduleDate;
			RepeatJob.this.nextDue = scheduleDate;
			RepeatJob.this.retry = false;
			RepeatJob.this.failed = false;
		}
		
		/* (non-Javadoc)
		 * @see org.oddjob.treesched.ScheduleListener#retry(java.util.Date, java.util.Date)
		 */
		@Override
		public void retry(Date scheduleDate, Date retryDate) {
			RepeatJob.this.scheduleDate = scheduleDate;
			RepeatJob.this.nextDue = retryDate;
			RepeatJob.this.retry = true;
			RepeatJob.this.failed = false;
		}
		
		/* (non-Javadoc)
		 * @see org.oddjob.treesched.ScheduleListener#failed(java.util.Date)
		 */
		public void failed(Date scheduleDate) {
			RepeatJob.this.scheduleDate = scheduleDate;
			RepeatJob.this.nextDue = scheduleDate;
			RepeatJob.this.retry = false;
			RepeatJob.this.failed = true;
		}
	}
	
    class ScheduleStateListener implements StateListener {
		
    	State state;
    	
    	@Override
    	public final void jobStateChange(StateEvent event) {
    		state = event.getState();
    	}
	}
    
	/**
	 * @return Returns the nextDue.
	 */
	public Date getNextDue() {
		return nextDue;
	}
	/**
	 * @return Returns the schduleDate.
	 */
	public Date getScheduleDate() {
		return scheduleDate;
	}
	/**
	 * @param schduleDate The schduleDate to set.
	 */
	public void setScheduleDate(Date schduleDate) {
		this.scheduleDate = schduleDate;
	}
	
}
