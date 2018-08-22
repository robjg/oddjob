package org.oddjob.schedules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @oddjob.description Provide a schedule based on a list of schedules. All schedules are
 * evaluated and that schedule which is due to start first is used.
 * 
 * 
 * @oddjob.example
 * 
 * Schedule on Monday and a Friday.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/ScheduleListExample.xml}
 * 
 * @author Rob Gordon
 */

final public class ScheduleList implements Serializable, Schedule {
    private static final long serialVersionUID = 20051125;
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduleList.class);
    
    /** 
     * @oddjob.property
     * @oddjob.description The list of schedules. 
     * @oddjob.required No, but pointless if missing.
     */
	private final List<Schedule> schedules =
		new ArrayList<Schedule>();

	public void setSchedules(int index, Schedule schedule) {
		if (schedule == null) {
			schedules.remove(index);
		}
		else {
			schedules.add(index, schedule);
		}
	}
	
	public Schedule getSchedules(int index) {
		return schedules.get(index);
	}
	
	public void setSchedules(Schedule[] schedules) {
		this.schedules.clear();
		this.schedules.addAll(Arrays.asList(schedules));
	}

	public Schedule[] getSchedules() {
		return this.schedules.toArray(new Schedule[0]);
	}	
	
	/**
	 * The number of subschedules this list contains.
	 * 
	 * @return The number of subschedules.
	 */
	public int size() {
		return schedules.size();	
	}
		
	/*
	 *  (non-Javadoc)
	 * @see org.treesched.Schedule#nextDue(java.util.Date)
	 */
	public ScheduleResult nextDue(ScheduleContext context) {
		Date now = context.getDate();
		
		logger.debug(this + ": in date " + now);
		
		if (schedules == null || schedules.size() == 0) {
			return null;
		}
		
		ScheduleResult candidate = null;
		
		int i = 1;		
		for	(Schedule schedule : schedules) {
			
			logger.debug(this + ": evaluating schedule " + i++ + " (" + schedule + ")");
			
			ScheduleResult nextDue = schedule.nextDue(context);
			
			if (nextDue != null) {
			    if (candidate == null || new IntervalHelper(
			    		nextDue).isBefore(candidate)) {
			        candidate = nextDue;
			    }
			}
		}
		
		logger.debug(this + ": returning " + candidate);
		
		return candidate;
	}

	/**
	 * Override toString to be more useful.
	 * 
	 * @return A description of the schedule.
	 */
	public String toString() {
		return "ScheduleList, size=" + size();
	}
}
