package org.oddjob.schedules;

import java.io.Serializable;

/**
 * Provide a basis of common functionality for schedules.
 * 
 * @author Rob Gordon
 */
abstract public class AbstractSchedule
		implements Serializable, RefineableSchedule {

    private static final long serialVersionUID = 20050226;
    
	/** A child schedule */
	private Schedule childSchedule;

	/**
	 * @oddjob.property refinement
	 * @oddjob.description Provide a refinement to this schedule.
	 * @oddjob.required No.
	 * 
	 * @param The refinement.
	 */
	public void setRefinement(Schedule childSchedule) {		
		this.childSchedule = childSchedule;
	}
	
	/**
	 * Return the child schedule.
	 * 
	 * @return The child schedule.
	 */	
	public Schedule getRefinement() {	
		return childSchedule;
	}

}
