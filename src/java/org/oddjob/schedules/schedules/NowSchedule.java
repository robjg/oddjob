/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;

import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalHelper;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.SimpleScheduleResult;

/**
 * @oddjob.description Schedule something now. 
 * 
 * @author Rob Gordon.
 */
public class NowSchedule implements Schedule {
	
    /* 
     * Due immediately.
     */
    public ScheduleResult nextDue(ScheduleContext context) {
		IntervalTo now = new IntervalTo(context.getDate());
		Interval parentInterval = context.getParentInterval();
		if (parentInterval == null) {
			return now;
		}

		Interval limited = new IntervalHelper(parentInterval).limit(now);
		if (limited == null) {
			return null;
		}
		else {
			return new SimpleScheduleResult(limited);
		}
    }
    
   /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	return "Now";
    }
    
}
