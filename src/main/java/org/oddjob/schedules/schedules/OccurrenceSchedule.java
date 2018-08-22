package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.schedules.AbstractSchedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;

/**
 * @oddjob.description This schedule counts the occurence's 
 * of it's nested schedule
 * and returns the required occurrence.
 * 
 * @oddjob.example
 * 
 * Second Tuesday of the month.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/OccurenceScheduleExample.xml}
 * 
 * @author Rob Gordon
 */

final public class OccurrenceSchedule extends AbstractSchedule implements Serializable {

    private static final long serialVersionUID = 20050226;
    
    private final static Logger logger = LoggerFactory.getLogger(OccurrenceSchedule.class);
    
    /**
     * @oddjob.property
     * @oddjob.description The number of the required occurrence.
     * @oddjob.required Yes.
     */
	private int occurrence;
	
	/**
	 * Set the number of the occurrence for this schedule.
	 * 
	 * @param occurrence The occurence.
	 */
	public void setOccurrence(String occurrence) {
		this.occurrence = Integer.parseInt(occurrence);
	}

	/**
	 * Return the number of the occurrence for this schedule.
	 * 
	 * @return The occurrence.
	 */
	
	public String getOccurrence() {		
		return Integer.toString(occurrence);
	}
	
	/**
	 * Return the next due interval which is the given occurrence of
	 * it's child schedules.
	 */
	
	public ScheduleResult nextDue(ScheduleContext context) {
		Date now = context.getDate();
		if (getRefinement() == null) {
		    throw new IllegalStateException("Occurence must have a child schedule.");
		}

		logger.debug(this + ": in Date is " + now);
		
	    Date use = now;
	    // start from the beginning interval of our parent, if there is one.
		if (context.getParentInterval() != null) {
		    use = context.getParentInterval().getFromDate();
		}
		ScheduleResult candidate = null;
		
		for (int i = 0; i < occurrence && use != null; ++i) {
			logger.debug(this + ": use interval is " + use);
			candidate = getRefinement().nextDue(context.move(use));
			
			if (candidate != null) {
				use = candidate.getToDate();
			}
			else {				
				// break the cycle
				use = null;
			}
		}
		
		return candidate;
	}

	/**
	 * Override toString.
	 */
	
	public String toString() {
		
		return "Occurence Schedule, occurences " + getOccurrence();
	}
}
