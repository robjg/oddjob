/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;

import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;

/**
 * @oddjob.description Schedule something now. 
 * 
 * @author Rob Gordon.
 */
public class NowSchedule implements Schedule {
	
    /* 
     * Due immediately.
     */
    public IntervalTo nextDue(ScheduleContext context) {
		return new IntervalTo(context.getDate());
    }
    
   /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	return "Now";
    }
    
}
