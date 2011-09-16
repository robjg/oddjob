package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.oddjob.schedules.AbstractSchedule;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalHelper;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.SimpleScheduleResult;

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
	public ScheduleResult nextDue(ScheduleContext context) {
		Date now = context.getDate();
		if (now == null) {
	        return null;
	    }
		if (getRefinement() == null) {
		    throw new IllegalStateException("Last must have a child schedule.");
		}
		
		logger.debug(this + ": in date is " + now);
		
		ScheduleResult last = null;						
		Date use = now;
		
		Schedule child = getRefinement();
		
		while(true) {
			logger.debug(this + ": use date is " + use);
			ScheduleResult candidate = child.nextDue(context.move(use));
			
			if (candidate == null) {
			    break;
			}
			last = candidate;
			use = candidate.getToDate();
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
