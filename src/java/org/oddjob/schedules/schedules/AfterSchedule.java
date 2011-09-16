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
 * @oddjob.description Schedule at a point in time immediately
 * after the nested schedule. 
 * <p>
 * This can be useful when wanting a schedule to begin at the end of an
 * interval instead of the beginning.
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
 * @author Rob Gordon
 */
final public class AfterSchedule extends AbstractSchedule implements Serializable {
    
    private static final long serialVersionUID = 20050226;
    
    private static final Logger logger = Logger.getLogger(AfterSchedule.class);
    
    private Schedule apply;
    
	public ScheduleResult nextDue(ScheduleContext context) {
				
		Schedule child = getRefinement();
		if (child == null) {
		    throw new IllegalStateException("After must have a child schedule.");
		}
		
		Date now = context.getDate();
		
		logger.debug(this + ": in date is " + now);
		
		ScheduleResult next = child.nextDue(context);
				
		if (next == null) {
		    return null;
		}
		
		Date from = next.getToDate();
		
		ScheduleResult following = child.nextDue(context.move(from));
		
		Date to;
		
		if (following == null) {
			to = new Date(IntervalTo.END_OF_TIME);
		}
		else {
			to = following.getToDate();
		}
		
		IntervalTo afterInterval = new IntervalTo(from, to); 
		
		Interval result; 
		if (apply == null) {
			result = afterInterval;
		}
		else {
			result = apply.nextDue(context.spawn(
					afterInterval.getFromDate(), afterInterval));
		}
		
		if (result == null) {
			return null;
		}
		
		return new SimpleScheduleResult(result, from);
	}
	
	public Schedule getApply() {
		return apply;
	}

	public void setApply(Schedule apply) {
		this.apply = apply;
	}
}
