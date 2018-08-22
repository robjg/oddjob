package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalHelper;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.ScheduleType;
import org.oddjob.schedules.SimpleScheduleResult;

/**
 * @oddjob.description This schedule allows a normal schedule 
 * to be broken by the results of another
 * schedule. This might be a list of bank holidays, or time of day, or any other
 * schedule.
 * <p>
 * This schedule works by moving the schedule forward if the start time of the
 * next interval falls within the next interval defined by the break. In the
 * example below for a time of Midday on 24th of December the logic is as follows:
 * <ul>
 *   <li>The schedule is next due at 10:00 on the 25th of December.</li>
 *   <li>This is within the break, move the schedule on.</li>
 *   <li>The schedule is next due at 10:00 on the 26th of December.</li>
 *   <li>This is within the break, move the schedule on.</li>
 *   <li>The schedule is next due at 10:00 on the 27th of December.</li>
 *   <li>This schedule is outside the break, use this result.</li>
 * </ul>
 * <p>
 * The optional alternative property defines a schedule to be used during the
 * breaks, instead of simply moving the interval forward.
 * 
 * @oddjob.example
 * 
 * A schedule that breaks for Christmas.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/BrokenScheduleExample.xml}
 * 
 * The logic is explained above.
 * 
 * @oddjob.example
 * 
 * A schedule with an alternative. The schedule breaks at weekends and for 
 * Christmas. During the break the schedule will be due once at 11am the
 * first day of the break, instead of the usual 10am.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/BrokenScheduleAlternative.xml}
 * 
 * 
 * @oddjob.example
 * 
 * Examples elsewhere.
 * <ul>
 *  <li>The {@link AfterSchedule} documentation has an example that uses the 
 *  <code>broken</code> schedule to calculate the day after the next
 *  working day.</li>
 *  <li>The {@link ScheduleType} documentation shows a <code>broken</code>
 *  schedule being used to calculate the next working day.</li>
 *  <li>The {@link DayAfterSchedule} and {@link DayBeforeSchedule} documentation 
 *  shows a <code>broken</code> schedule being used to move the last day of the month.</li>
 * </ul>
 * 
 * 
 * @author Rob Gordon
 */

public class BrokenSchedule implements Serializable, Schedule{

    private static final long serialVersionUID = 20050226;
    
    private static final Logger logger = LoggerFactory.getLogger(BrokenSchedule.class);
    
    /** 
     * @oddjob.property
     * @oddjob.description The schedule. 
     * @oddjob.required Yes.
     */
	private Schedule schedule;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The breaks. 
	 * @oddjob.required No, but this schedule is pointless if none are provided.
	 */
	private Schedule breaks;

	/**
	 * @oddjob.property
	 * @oddjob.description An alternative schedule to apply during a break.
	 * The alternative schedule will be passed the interval that is the break.
	 * @oddjob.required No.
	 */
	private Schedule alternative;
	
	/**
	 * Set the schedule to break up.
	 * 
	 * @param schedule The schedule to break up.
	 */
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	/**
	 * Get the schedule to break up.
	 * 
	 * @return The schedule to break up.
	 */
	public Schedule getSchedule() {
		return this.schedule;
	}
		
	/**
	 * Set the breaks which will break up the schedule.
	 * 
	 * @param breaks The breaks schedule.
	 */
	public void setBreaks(Schedule breaks) {		
		this.breaks = breaks;
	}
	
	/**
	 * Get the breaks which will break up the schedule.
	 * 
	 * @return The break Schedule.
	 */
	
	public Schedule getBreaks() {
		return this.breaks;
	}

	public Schedule getAlternative() {
		return alternative;
	}

	public void setAlternative(Schedule alternative) {
		this.alternative = alternative;
	}

	/**
	 * Implement the schedule.
	 */
	public ScheduleResult nextDue(ScheduleContext context) {		
		Date now = context.getDate();

		logger.debug(this + ": in interval is " + now);

		// sanity checks
		if (schedule == null) {			
			return null;
		}

	    if (breaks == null) {
			return schedule.nextDue(context);
		}

		Date use = now; 

		// loop until we get a valid interval
		while (true) {
			if (use == null) {
				return null;
			}

			ScheduleResult next = schedule.nextDue(context.move(use));	
			// if the next schedule is never due return.
			if (next == null) {
				return null;
			}

			// find the first exclusion interval
			Interval exclude = mergeBreaks(context.move(next.getFromDate()));
				
			if (exclude == null) {
			    return next;
			}
			
			// if this interval is before the break
			if (new IntervalHelper(next).isBefore(exclude)) {
				return next;				
			}
			
			// if we got here the last interval is blocked by an exclude.
			
			Date lastUse = use;
			
			// move the interval on.
			if (next.getUseNext() == null) {
				use = null;
			}
			else {
				if (exclude.getToDate().after(next.getUseNext())) {
					use = exclude.getToDate();
				}
				else {
					use = next.getUseNext();
				}
			}
			
			// see if there is an alternative.
			if (alternative != null) {
				// If the interval is ahead of now move now.
				if (lastUse.before(exclude.getFromDate())) {
					lastUse = exclude.getFromDate();
				}
				ScheduleResult alternativeResult = alternative.nextDue(
						context.spawn(lastUse, exclude));
				if (alternativeResult != null) {
					return new SimpleScheduleResult(alternativeResult, use);
				}
				// If the alternative is null, move on.
			}			
		}
	}

	private Interval mergeBreaks(ScheduleContext context) {
		
		ScheduleContext useContext = context;
		
		Interval merged = null;
		while (true) {
			Interval exclude = breaks.nextDue(useContext);
			if (exclude == null) {
				return merged;
			}
			if (merged == null) {
				merged = exclude;
			}
			else {
				if (exclude.getFromDate().after(merged.getToDate())) {
					return merged;
				}
				
				merged = new IntervalTo(merged.getFromDate(), exclude.getToDate());
			}
			
			useContext = useContext.move(merged.getToDate());
		}
	}
	
	
	
	/**
	 * Provide a simple string description.
	 */	
	public String toString() {
		return "Broken Schedule " + schedule + " with breaks " + breaks;
	}
}
