package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.oddjob.schedules.AbstractSchedule;
import org.oddjob.schedules.DateUtils;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;

/**
 * @oddjob.description This schedule will return it's last due nested 
 * schedule within the given parent interval.
 * 
 * @oddjob.example
 * 
 * Last Tuesday or Wednesday of the month, whichever is last.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/LastExample.xml}
 * 
 * 
 * @author Rob Gordon
 */
final public class LastSchedule extends AbstractSchedule implements Serializable {

    private static final long serialVersionUID = 20050226;
    
    private static final Logger logger = Logger.getLogger(LastSchedule.class);
    
	/**
	 * Calculate the next due interval within the given interval.
	 */
	public IntervalTo nextDue(ScheduleContext context) {
		Date now = context.getDate();
		if (now == null) {
	        return null;
	    }
		if (getRefinement() == null) {
		    throw new IllegalStateException("Last must have a child schedule.");
		}
		
		logger.debug(this + ": in date is " + now);
		
		IntervalTo last = null;						
		Date use = now;
		
		Schedule child = getRefinement();
		
		while(true) {
			logger.debug(this + ": use date is " + use);
			IntervalTo candidate = child.nextDue(context.move(use));
			if (context.getParentInterval() != null) {
				candidate = context.getParentInterval().limit(candidate);
			}
			
			if (candidate == null) {
			    break;
			}
			last = candidate;
			use = candidate.getUpToDate();
		}
		
		return last;
	}

	/**
	 * Override toString.
	 */
	
	public String toString() {
		return "Last Schedule";
	}

}
