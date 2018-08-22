package org.oddjob.util;

import java.util.Date;
import java.util.TimeZone;

/**
 * Something that can provide a date.
 * 
 * @See DateShortcuts
 * 
 * @author rob
 *
 */
public interface DateProvider {

	/**
	 * Provider the date given the clock and the time zone.
	 * 
	 * @param clock The clock. Must not be null.
	 * @param timeZone The time zone. Null indicates the current time zone.
	 * 
	 * @return The date this provider provides.
	 */
	public Date dateFor(Clock clock, TimeZone timeZone);
}
