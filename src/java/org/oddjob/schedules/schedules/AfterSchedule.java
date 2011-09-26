package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.oddjob.schedules.AbstractSchedule;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.SimpleScheduleResult;

/**
 * @oddjob.description Schedule something after the given schedule. 
 * <p>
 * This can be useful when wanting a schedule to begin at the end of an
 * interval instead of the beginning, or for scheduling around holidays when 
 * a process is still required to run on the holiday, but not the day after 
 * the holiday.
 * <p>
 * The after schedule differs from the {@link DayAfterSchedule} in that
 * day-after is designed to narrow it's parent interval but this schedule 
 * applies a refinement to it child schedule. The difference is subtle
 * but hopefully the examples demonstrate how each should be used. 
 * 
 * @oddjob.example
 * 
 * A schedule for the end of the interval.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/AfterScheduleExample.xml}
 * 
 * This would schedule a job to run once after 20 minutes. It could be
 * used to stop a long running job for instance.
 * 
 * @oddjob.example
 * 
 * A schedule for the day after a the current business day.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/AfterBusinessDays.xml}
 * 
 * Normally this will schedule something from 08:00 am Tuesday to Saturday, 
 * but for the week where Monday 2nd of May was a public holiday the schedule
 * will be from Wednesday to Saturday.
 * 
 * @author Rob Gordon
 */
final public class AfterSchedule extends AbstractSchedule implements Serializable {
    
    private static final long serialVersionUID = 200902262011092000L;
    
    private static final Logger logger = Logger.getLogger(AfterSchedule.class);
    
	/**
	 * @oddjob.property
	 * @oddjob.description The schedule to be after.
	 * @oddjob.required Yes.
	 */
    private Schedule schedule;
    
	public ScheduleResult nextDue(ScheduleContext context) {
				
		if (schedule == null) {
		    throw new IllegalStateException("After must have a schedule to be after.");
		}
		
		Date now = context.getDate();
		
		logger.debug(this + ": in date is " + now);
		
		ScheduleResult next = schedule.nextDue(context);
				
		if (next == null) {
		    return null;
		}
		
		Date from = next.getToDate();
		
		ScheduleResult following = schedule.nextDue(context.move(from));
		
		Date to;
		
		if (following == null) {
			to = Interval.END_OF_TIME;
		}
		else {
			to = following.getToDate();
		}
		
		IntervalTo afterInterval = new IntervalTo(from, to); 
		
		Schedule refinement = getRefinement();  
		Interval result; 
		if (refinement == null) {
			result = afterInterval;
		}
		else {
			result = refinement.nextDue(context.spawn(
					afterInterval.getFromDate(), afterInterval));
		}
		
		if (result == null) {
			return null;
		}
		
		return new SimpleScheduleResult(result, from);
	}
	
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
}
