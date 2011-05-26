/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * A schedule context provides a context for the evaluation of
 * a schedules next due interval.
 */
public class ScheduleContext {

	/** The date for the evaluation */
	private final Date date;
	
	/** The time zone the schedule is to be evaluated in. */
	private final TimeZone timeZone;
	
	/** A data map which allows schedules which maintain state to use
	 * to maintain that state.
	 */
	private final Map<Object, Object> data;
		
	private final IntervalTo parentInterval;
	
	/**
	 * Constructor for a new context with a date to evaluate from, and using 
	 * the default time zone.
	 * 
	 * @param now The date to evaluate from.
	 */
	public ScheduleContext(Date now) {
		this(now, null, null, null);
	}
	
	/**
	 * Constructor for a new context with a date to evaluate from, and a
	 * time zone to evaluate the schedule with.
	 * 
	 * @param use The date to evaluate from.
	 * @param timeZone The time zone.
	 */
	public ScheduleContext(Date use, TimeZone timeZone) {
		this(use, timeZone, null, null);		
	}
	
	/**
	 * Constructor with a data map.
	 * 
	 * @param now The date to evaluate from. A null date is allowed
	 * and means the schedule will never be due.
	 * @param timeZone The time zone.
	 * @param data The data map.
	 */
	public ScheduleContext(Date now, TimeZone timeZone, 
			Map<Object, Object> data) {
		this(now, timeZone, data, null);
	}
	
	/**
	 * Constructor with a data map.
	 * 
	 * @param now The date to evaluate from. A null date is allowed
	 * and means the schedule will never be due.
	 * @param timeZone The time zone.
	 * @param data The data map.
	 */
	public ScheduleContext(Date now, TimeZone timeZone, 
			Map<Object, Object> data, IntervalTo parentInterval) {

		if (now == null) {
			throw new NullPointerException("Date is Null.");
		}
		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}
		if (data == null) {
			data = new HashMap<Object, Object>();
		}
		
		this.date = now;
		this.timeZone = timeZone;
		this.data = data;
		this.parentInterval = parentInterval;
	}
	
	/**
	 * Get the date to evaluate the schedule with.
	 * 
	 * @return The date to evaluate the schedule with. Never null.
	 */
	public Date getDate() {
		return date;
	}
		
	/**
	 * Get the time zone to evaluate the schdule in. 
	 * 
	 * @return The time zone, Never null.
	 */
	public TimeZone getTimeZone() {
		return timeZone;
	}
	
	/**
	 * Add data to the context data map.
	 * 
	 * @param key The key.
	 * @param value The value.
	 */
	public void putData(Object key, Object value) {
		data.put(key, value);
	}
	
	/**
	 * Get data back from the context data map.
	 * 
	 * @param key The key.
	 * @return The value.
	 */
	public Object getData(Object key) {
		return data.get(key);
	}
	
	public IntervalTo getParentInterval() {
		return parentInterval;
	}
	
	/**
	 * Create a new context with the existing time zone and data map,
	 * but a new parent interval. This is used when narrowing
	 * a schedule with a refinement.
	 *  
	 * @param parentInterval The new constraining parent interval.
	 * 
	 * @return A new context. Never null.
	 */
	public ScheduleContext spawn(IntervalTo parentInterval) {
		ScheduleContext newContext = new ScheduleContext(this.date, 
				this.timeZone, this.data, parentInterval); 
		return newContext;
	}
	
	/**
	 * Create a new context from the existing one for a new
	 * parent interval and with an new context date. Used when a
	 * evaluating a next date in a refinement.
	 * 
	 * @param date The next date.
	 * @param parentInterval The new constraining parent interval.
	 * 
	 * @return A new context. Never null.
	 */
	public ScheduleContext spawn(Date date, IntervalTo parentInterval) {
		ScheduleContext newContext = new ScheduleContext(date, 
				this.timeZone, this.data, parentInterval); 
		return newContext;
	}
	
	/**
	 * Moves the existing context to one for a new date. The
	 * parent interval, time zone and data map remain unchanged.
	 * 
	 * @param date The date to shift the context to.
	 * 
	 * @return A new context. never null.
	 */
	public ScheduleContext move(Date date) {
		ScheduleContext newContext = new ScheduleContext(date, 
				this.timeZone, this.data, this.parentInterval); 
		return newContext;
	}
	
	public String toString() {
		return "Context Date: " + date + 
				( parentInterval == null ? 
						"" : ", Parent Interval [" + parentInterval + "]") + 
				( timeZone == null ? 
						"" : ", Time Zone " + timeZone.getID()); 
	}
}
