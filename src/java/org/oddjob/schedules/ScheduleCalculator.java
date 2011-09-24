package org.oddjob.schedules;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.oddjob.schedules.schedules.CountSchedule;
import org.oddjob.schedules.schedules.NowSchedule;
import org.oddjob.util.Clock;

/**
 * A class capable of calculating next due times for a job by using two
 * schedules - a normal schedule for normal completion and a retry schedule
 * for when a job hasn't completed. 
 * 
 * @author Rob Gordon
 */
public class ScheduleCalculator {
	private static final Logger logger = Logger.getLogger(ScheduleCalculator.class);
	/**
	 * The normal schedule.
	 */
	private final Schedule normalSchedule;

	/**
	 * The Retry schedule.
	 */
	private final Schedule retrySchedule;

	
	/** The current schedule being used. */
	private Schedule currentSchedule;
				
	/** The current/last interval in which schedule is due */
	private ScheduleResult currentInterval;

	/** last normal interval from the regular schedule */
	private ScheduleResult normalInterval;

	/** Initialised */
	private boolean initialised;

	/** Listeners. */
	private final List<ScheduleListener> listeners = 
		new ArrayList<ScheduleListener>();
	
	/** Current schedule context */
	private ScheduleContext normalContext;
	/** Retry schedule context */
	private ScheduleContext retryContext;
	
	/** The time zone. */
	private final TimeZone timeZone;
	
	private final Clock clock;
	
	/**
	 * Constructor for a calculator with no retry for the default time zone. 
	 * 
	 * @param clock The clock to use, may not be null.
	 * @param schedule The normal schedule, may not be null.
	 */
	public ScheduleCalculator(Clock clock, Schedule schedule) {
		this(clock, schedule, null, null);
	}
	
	/**
	 * Constructor for a calculator with with a timeZone. 
	 * 
	 * @param clock The clock to use, may not be null.
	 * @param schedule The normal schedule, may not be null.
	 * @param timeZone The time zone. May be null
	 */
	public ScheduleCalculator(Clock clock, Schedule schedule, TimeZone timeZone) {
		this(clock, schedule, null, timeZone);
	}
	
	/**
	 * Constructor for a calculator with a retry schedule using the default time zone. 
	 * 
	 * @param clock The clock to use, may not be null.
	 * @param schedule The normal schedule, may not be null.
	 * @param retry The retrySchedule. May be null. 
	 */
	public ScheduleCalculator(Clock clock, Schedule schedule, Schedule retry) {
		this(clock, schedule, retry, null);
	}
	
	/**
	 * Constructor for a calculator with a retry schedule and a timeZone. 
	 * 
	 * @param clock The clock to use, may not be null.
	 * @param schedule The normal schedule, may not be null.
	 * @param retry The retrySchedule. May be null. 
	 * @param timeZone The time zone. May be null
	 */
	public ScheduleCalculator(Clock clock, Schedule schedule, Schedule retry, TimeZone timeZone) {
		if (clock == null) {
			throw new IllegalStateException("Null clock not allowed.");			
		}
		if (schedule == null) {
			schedule = defaultSchedule();
		}
		
		this.clock = clock;
		this.normalSchedule = schedule;
		this.retrySchedule = retry;
		this.timeZone = timeZone;
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
	 * Getter for retry.
	 * 
	 * @return The retry schedule.
	 */
	public Schedule getRetry() {
	    return this.retrySchedule;
	}

	public void initialise() {
		initialise(null, new HashMap<Object, Object>());
	}
	
	/**
	 * Initialize the scheduler.
	 */
	synchronized public void initialise(ScheduleResult lastComplete, Map<Object, Object> contextData) {
		if (initialised) {
			throw new IllegalStateException("Already initialised.");
		}
		Date nowTime = clock.getDate(); 
		logger.debug("Initialising, time now [" + nowTime + "], lastComplete [" + lastComplete + "]");

		// always start on a normal schedule.
		currentSchedule(normalSchedule);
		// if the last complete time was persisted.
		if (lastComplete != null && lastComplete.getUseNext() != null) {
			// work with a time that is one millisecond after the lastComplete
			// interval.
			Date useTime = lastComplete.getUseNext(); 
			normalContext = new ScheduleContext(
					useTime, timeZone, contextData);
		    currentInterval = currentSchedule.nextDue(normalContext);
			normalInterval = currentInterval;
		    fireInitialised();
        } 
		else {
	        logger.debug("Starting up with no last complete date.");
	        normalContext = new ScheduleContext(
	        		nowTime, timeZone, contextData);
			currentInterval = currentSchedule.nextDue(normalContext);
			normalInterval = currentInterval;
			fireInitialised();
		}
        initialised = true;
	}

	
	/**
	 * Change the current schedule.
	 * 
	 * @param schedule The current schedule.
	 */
	private void currentSchedule(Schedule schedule) {
		this.currentSchedule = schedule;
	}
	
	/**
	 * Get the name of the current schedule.
	 * 
	 * @return The name of the current schedule.
	 */
	synchronized public String getCurrentScheduleType() {
		if (currentSchedule == normalSchedule) {
			return "Normal";
		}
		else if (currentSchedule == retrySchedule) {
			return "Retry";
		}
		else {
			return "Undefined";
		}
	}	
	
	synchronized public void calculateComplete() {
		
		logger.debug("Calculate Complete");
		currentSchedule(normalSchedule);
		ScheduleResult lastComplete = normalInterval;
		normalContext = normalContext.move(
				currentInterval.getToDate());
		// calculate the next due time.
		currentInterval = currentSchedule.nextDue(
				normalContext);
		normalInterval = currentInterval;
		
		fireComplete(lastComplete);
	}

	/**
	 * Calculate the retry schedule.
	 *
	 */
	synchronized public void calculateRetry() {
		logger.debug("Calculate Retry");
	  	if (currentSchedule == normalSchedule) {
	        // job just failed
			if (retrySchedule != null) {
		        logger.debug("Switching to retry schedule.");
			    normalInterval = currentInterval;
				currentSchedule(retrySchedule);
				// use the current time
				retryContext = new ScheduleContext(clock.getDate(),
						timeZone);
				
				// add parent limits to the context. Used by Interval.
			    if (!new IntervalHelper(normalInterval).isPoint()) {
			    	retryContext = retryContext.spawn(normalInterval);
			    }
			    
			    retryAndFail();
			} 
			else {
				// no retry
				normalContext = normalContext.move(normalInterval.getToDate());
		        currentInterval = normalSchedule.nextDue(normalContext);
		        normalInterval = currentInterval;
		        fireFailed();
			}
	    }
	  	else {
	  		retryContext = retryContext.move(currentInterval.getToDate());
	  		
	  		retryAndFail();
	  	}	  	
        logger.debug("Next inteval is [" + currentInterval + "]");
	}

	private void retryAndFail() { 
		
        currentInterval = retrySchedule.nextDue(retryContext);
        
	    
        if (currentInterval != null) {
        	
        	Interval retryInterval = currentInterval;
        	
            // if the normal interval isn't a point then use it to
            // limit the retry schedule.
            IntervalHelper helper = new IntervalHelper(retryInterval);
            
    	    if (!helper.isPoint()) {
    	    	retryInterval = helper.limit(currentInterval);
    	    }
    	    
	        fireRetry(retryInterval);
        } 
        else {
        	// else fail
			currentSchedule(normalSchedule);
			normalContext = normalContext.move(normalInterval.getUseNext());
	        currentInterval = normalSchedule.nextDue(normalContext);
	        normalInterval = currentInterval;
            logger.debug("Switched back to normal schedule and next interval is [" + currentInterval + "]");	        
	        fireFailed();
        }
	}
	
	
		
	synchronized public void addScheduleListener(ScheduleListener l) {
		listeners.add(l);
	}
	
	synchronized public void removeScheduleListener(ScheduleListener l) {
		listeners.remove(l);
	}
	
	protected void fireInitialised() {
		Date scheduleDate;
	    if (normalInterval == null) {
	        scheduleDate = null;
	    }
	    else {
	        scheduleDate = normalInterval.getFromDate();
	    }
	    
		for (Iterator<ScheduleListener> it = listeners.iterator(); it.hasNext(); ) {
			ScheduleListener l = (ScheduleListener) it.next();
			l.initialised(scheduleDate);
		}
	}
	    
	protected void fireComplete(ScheduleResult lastComplete) {
		Date scheduleDate;
	    if (normalInterval == null) {
	        scheduleDate = null;
	    }
	    else {
	        scheduleDate = normalInterval.getFromDate();
	    }
	    
		for (Iterator<ScheduleListener> it = listeners.iterator(); it.hasNext(); ) {
			ScheduleListener l = (ScheduleListener) it.next();
			l.complete(scheduleDate, lastComplete);
		}		
	}
	
	protected void fireRetry(Interval limits) {
	    if (normalInterval == null) {
	        throw new IllegalStateException("Can't retry without have a normal interval!");
	    }
	    if (limits == null) {
	        throw new IllegalStateException("Can't retry without have an interval!");
	    }

	    Date scheduleDate = normalInterval.getFromDate();
		Date retryDate = limits.getToDate();
	        
		for (Iterator<ScheduleListener> it = listeners.iterator(); it.hasNext(); ) {
			ScheduleListener l = (ScheduleListener) it.next();
			l.retry(scheduleDate, retryDate);
		}		
	}
	
	protected void fireFailed() {
		Date scheduleDate;
	    if (normalInterval == null) {
	        scheduleDate = null;
	    }
	    else {
	        scheduleDate = normalInterval.getFromDate();
	    }
		for (Iterator<ScheduleListener> it = listeners.iterator(); it.hasNext(); ) {
			ScheduleListener l = (ScheduleListener) it.next();
			l.failed(scheduleDate);
		}		
	}
	
	private Schedule defaultSchedule() {
		
		CountSchedule count = new CountSchedule();
		count.setCount("1");
		
		count.setRefinement(new NowSchedule());
		
		return count;
	}
}

