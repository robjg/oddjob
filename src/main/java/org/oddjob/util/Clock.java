package org.oddjob.util;

import java.util.Date;

/**
 * A clock is used by a schedule to tell the time. The clock could
 * use the system time or the time in a different time zone, or a
 * time server, or some other means of telling the time.
 * 
 * @author Rob Gordon
 */

public interface Clock {

	/**
	 * Return the java date for the current time by the clock.
	 * 
	 * @return The date.
	 */
	Date getDate();
}
