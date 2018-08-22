package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalHelper;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.SimpleScheduleResult;

/**
 * @oddjob.description This schedule returns an interval 
 * from the given time to the interval time later.
 * <p>
 * This schedule is commonly used as a refinement of another schedule 
 * such as the {@link DailySchedule}, {@link TimeSchedule} or {@link CountSchedule}
 * schedules.
 * 
 * @oddjob.example
 * 
 * Every 20 minutes.
 *
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/IntervalExample.xml}
 * 
 * @oddjob.example
 * 
 * Examples Elsewhere.
 * <ul>
 *  <li>{@link TimeSchedule}</li>
 *  <li>{@link DailySchedule}</li>
 *  <li>{@link CountSchedule}</li>
 * </ul>
 * 
 * @author Rob Gordon
 */
public class IntervalSchedule implements Schedule, Serializable {

    private static final long serialVersionUID = 20050226;
    
    private static Logger logger = LoggerFactory.getLogger(IntervalSchedule.class);
    
	/**
	 * Milliseconds for the interval.
	 */
	private long intervalMillis;

	/**
	 * Default bean constructor.
	 */
	public IntervalSchedule() {
	}
	
	/**
	 * Constructor with milliseconds.
	 * 
	 * @param millis
	 */
	public IntervalSchedule(long millis) {
		intervalMillis = millis;
	}

	/**
	 * @oddjob.property interval
	 * @oddjob.description The interval time. The interval must be specified
	 * in one of the formats:
	 * <dl>
	 * <dt>hh:mm</dt><dd>Hours and minutes.</dd>
	 * <dt>hh:mm:ss</dt><dd>Hours, minutes and seconds.</dd>
	 * <dt>hh.mm.ss.SSS</dt><dd>Hours, minutes, seconds and milliseconds.</dd>
	 * </dl>
	 * @oddjob.required No but defaults to no interval.
	 * 
	 * @param interval The interval.
	 * @throws ParseException If the interval is not a valid date.
	 */
	public void setInterval(String interval) throws ParseException {						
		intervalMillis = DateHelper.parseTime(interval);
	}

    /*
     *  (non-Javadoc)
     * @see org.treesched.Schedule#nextDue(java.util.Date)
     */
	public ScheduleResult nextDue(ScheduleContext context) {

		if (intervalMillis == 0) {
			throw new IllegalStateException("An interval of 0 is invalid.");
		}
		
		Date now = context.getDate();
		
		Interval parentInterval = context.getParentInterval();
		Interval nextInterval = null;
		
		if (parentInterval == null) {
		    // no limits so just return an interval later.
		    nextInterval = new IntervalTo(now, new Date(now.getTime() + intervalMillis));
		}
		else {
		    // if there is limits then calculate the interval 
		    // that might be due from the beginning.
		    Date start = parentInterval.getFromDate();
		    
		    // if before start - take the first interval.
			if (! (start.before(now))) {				
				nextInterval = new IntervalTo(start, 
				        new Date(start.getTime() + intervalMillis));
			}
			else {
			// else work out which interval we'er in.
				long sinceStart = now.getTime() - start.getTime();
				long intervals = sinceStart / intervalMillis;
				long lastBegin = start.getTime() + intervals * intervalMillis;

				nextInterval = new IntervalTo(
						new Date(lastBegin), 
						new Date(lastBegin + intervalMillis));
			}
			
			nextInterval = new IntervalHelper(
					parentInterval).limit(nextInterval);
		}
		logger.debug(this + ": in date is " + now
					+ ", next Interval is " + nextInterval);

		if (nextInterval == null) {
			return null;
		}
		else {
			return new SimpleScheduleResult(nextInterval);
		}
	}

	@Override
	public String toString() {
		return "Interval " + intervalMillis + " ms";
	}
}
