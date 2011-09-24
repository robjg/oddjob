package org.oddjob.schedules;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A representation of an interval between two dates.
 * 
 * @author Rob Gordon
 */

class IntervalBase implements Serializable {
    private static final long serialVersionUID = 20050226;
        
	private final Date fromDate;
	private final Date toDate;
	
	
	public IntervalBase(Date on) {
		this(on.getTime(), on.getTime());
	}
	
	/**
	 * Constructor for an interval between two dates.
	 * 
	 * @param from The starting date.
	 * @param to The ending date.
	 */
	public IntervalBase(Date from, Date to) {
		this(from.getTime(), to.getTime());
	}
	
	
	/**
	 * Constructor for an interval between two dates given as milliseconds.
	 * 
	 * @param fromTime
	 * @param toTime
	 */
	public IntervalBase(long fromTime, long toTime) {
		fromDate = new Date(fromTime);
		toDate = new Date(toTime);
		if (toTime < fromTime) {
			throw new IllegalStateException("An interval can not have a to [" +
					toDate + "] before the from [" + fromDate + "]");
		}
	}

	/**
	 * The copy constructor. This is a shallow copy.
	 *  
	 * @param other The other interval.
	 */
	public IntervalBase(IntervalBase other) {
		this(other.fromDate.getTime(), other.toDate.getTime());
	}

	/**
	 * Get the from date of the interval.
	 * 
	 * @return The from date.
	 */
	public Date getFromDate() {
		return fromDate;
	}
	
	/**
	 * Get the to date of the interval.
	 * 
	 * @return The to date.
	 */	
	protected Date getEndDate() {
		return toDate;
	}
	
	/**
	 * Test if this interval is before the other interval.
	 * <p> 
	 * This happens when this from time is less than other from time.
	 * <pre>
	 * this:    ----    or   ----
	 * other:    ----             ----
	 * </pre>
	 * @param other
	 * @return true if this is before the other.
	 */

	public boolean isBefore(IntervalBase other) {
	    if (other == null) {
	        return true;
	    }
		return this.fromDate.getTime() < other.fromDate.getTime();
	}
	
	/**
	 * Test if this interval is past the other interval.
	 * <p>
	 * This happens when the this from time is greater than the other ones to time.
	 * <pre>
	 * this:         ----
	 * other:   ----
	 * </pre>
	 * @return true if this is past the other.
	 */
	
	public boolean isPast(IntervalBase other) {
	    if (other == null) {
	        return true;
	    }
		return this.fromDate.getTime() > other.toDate.getTime();		
	}
			
	/**
	 * Is this interval really a point in time, not an interval.
	 * 
	 * @return true if it is a point in time.
	 */
	public boolean isPoint() {
		return fromDate.equals(toDate);
	}
	
	/**
	 * Return a string representation of this interval.
	 */		
	public String toString() {
		
		String fromString; 
		if (fromDate.getTime() % 1000 == 0) {
			// no milliseconds - then miss them off.
			fromString = new SimpleDateFormat(
					"dd-MMM-yyyy HH:mm:ss").format(fromDate);
		}
		else {
			fromString = new SimpleDateFormat(
					"dd-MMM-yyyy HH:mm:ss:SSS").format(fromDate);
		}
		
		String toString;
		if (toDate.getTime() + 1 % 1000 == 0) {
			// no milliseconds - then miss them off.
			toString = new SimpleDateFormat(
					"dd-MMM-yyyy HH:mm:ss").format(toDate);
		}
		else {
			toString = new SimpleDateFormat(
					"dd-MMM-yyyy HH:mm:ss:SSS").format(toDate);
		}
		
		return fromString + " to " + toString;	
	}
} 
