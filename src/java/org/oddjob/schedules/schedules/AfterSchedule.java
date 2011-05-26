package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.oddjob.schedules.AbstractSchedule;
import org.oddjob.schedules.DateUtils;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;

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
    
	public IntervalTo nextDue(ScheduleContext context) {
		Date now = context.getDate();
		if (getRefinement() == null) {
		    throw new IllegalStateException("After must have a child schedule.");
		}
		
		logger.debug(this + ": in date is " + now);
				
		Schedule child = getRefinement();

		Interval next = child.nextDue(context);
		if (next == null) {
		    return null;
		}
		
		Date after = DateUtils.oneMillisAfter(next.getToDate());
		if (after == null) {
			return null;
		}
		return new IntervalTo(after);
	}
}
