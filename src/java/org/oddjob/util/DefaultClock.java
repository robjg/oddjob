package org.oddjob.util;

import java.util.Date;

/**
 * Being phased out since moving to quartz.
 * 
 * @author Rob Gordon
 */

public class DefaultClock implements Clock {

    /**
	 * @oddjob.property date
	 * @oddjob.description The current date/time.
	 * @oddjob.required R/O
	 */
	public Date getDate() {
	    return new Date();
	}
	
	public String toString() {
	    return "DefaultClock: " + new Date(); 
	}

}
