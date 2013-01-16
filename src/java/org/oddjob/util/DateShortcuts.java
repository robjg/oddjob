package org.oddjob.util;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * A utility method to provide some common date shortcuts as 
 * {@link DateProvider}s.
 * 
 * @author rob
 *
 */
public class DateShortcuts {

	/** Map of DateProviders. */
	private static final Map<String, DateProvider> dateProviders = 
			new HashMap<String, DateProvider>();
	
	public enum Defaults implements DateProvider {
		
		/** Shortcut for the date now. */
		NOW {
			@Override
			public Date dateFor(Clock clock, TimeZone timeZone) {
				
				Calendar now = getCalendarInstance(timeZone);
				now.setTime(clock.getDate());
				
				return now.getTime();
			}
		},
		
		/** Shortcut for todays date (at midnight). */
		TODAY {
			@Override
			public Date dateFor(Clock clock, TimeZone timeZone) {
				
				Calendar now = getCalendarInstance(timeZone);
				now.setTime(clock.getDate());

				Calendar today = getCalendarInstance(timeZone);
				today.clear();
				today.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
						now.get(Calendar.DATE));
				
				return today.getTime();
			}
		},
		
		/** Shortcut for tomorrows date (at midnight). */
		TOMORROW {
			@Override
			public Date dateFor(Clock clock, TimeZone timeZone) {
				
				Calendar now = getCalendarInstance(timeZone);
				now.setTime(clock.getDate());

				Calendar tomorrow = getCalendarInstance(timeZone);
				tomorrow.clear();
				tomorrow.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
						now.get(Calendar.DATE) + 1);
				
				return tomorrow.getTime();
			}
		},
		
		/** Shortcut for yesterdays date (at midnight). */
		YESTERDAY {
			@Override
			public Date dateFor(Clock clock, TimeZone timeZone) {
				
				Calendar now = getCalendarInstance(timeZone);
				now.setTime(clock.getDate());

				Calendar yesterday = getCalendarInstance(timeZone);
				yesterday.clear();
				yesterday.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
						now.get(Calendar.DATE) - 1);
				
				return yesterday.getTime();
			}
		},
		
		;
	}
	
	/**
	 * Helper function to cope with null time zone.
	 * @param timeZone The time zone. May be null.
	 * 
	 * @return A calendar.
	 */
	static Calendar getCalendarInstance(TimeZone timeZone) {
		if (timeZone == null) {
			return Calendar.getInstance();
		}
		else {
			return Calendar.getInstance(timeZone);
		}
	}
	
	
	static {

		Defaults[] defaults = Defaults.values();
		for (Defaults _default : defaults) {
			dateProviders.put(_default.name(), _default);
		}
	}

	public static DateProvider getShortcut(String shortcut) {
		return dateProviders.get(shortcut);
	}
	
	public static void setProvider(String shortcut, DateProvider provider) {
		dateProviders.put(shortcut, provider);
	}
}
