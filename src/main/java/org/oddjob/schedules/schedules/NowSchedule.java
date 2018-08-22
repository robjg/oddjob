/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;

import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalHelper;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.ScheduleType;
import org.oddjob.schedules.SimpleInterval;
import org.oddjob.schedules.SimpleScheduleResult;

/**
 * @oddjob.description Schedule something now. 
 * 
 * @oddjob.example
 * 
 * Examples elsewhere. The {@link ScheduleType} examples show's <code>now</code>
 * being used to get the current date.
 * 
 * @author Rob Gordon.
 */
public class NowSchedule implements Schedule {
	
    /* 
     * Due immediately.
     */
    public ScheduleResult nextDue(ScheduleContext context) {
    	
    	// Create an result with the use next 1 millisecond behind the given 
    	// date. This is because a timer keeps using a millisecond after from the
    	// previous schedule, and so we can get to a situation where now
    	// is 1 millisecond away.
    	
		Interval now = new SimpleInterval(context.getDate());
		
		Interval parentInterval = context.getParentInterval();
		if (parentInterval == null) {
			return new SimpleScheduleResult(now, now.getFromDate());
		}

		Interval limited = new IntervalHelper(parentInterval).limit(now);
		if (limited == null) {
			return null;
		}
		else {
			return new SimpleScheduleResult(limited, now.getFromDate());
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
