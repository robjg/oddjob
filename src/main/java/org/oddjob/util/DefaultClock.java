package org.oddjob.util;

import java.util.Date;

/**
 * A default {@link Clock} that provides the current time.
 * 
 * @author Rob Gordon
 */

public class DefaultClock implements Clock {

	/*
	 * (non-Javadoc)
	 * @see org.oddjob.util.Clock#getDate()
	 */
	public Date getDate() {
	    return new Date();
	}
	
	public String toString() {
	    return "DefaultClock: " + new Date(); 
	}

}
