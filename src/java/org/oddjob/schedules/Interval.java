package org.oddjob.schedules;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A representation of an interval between two dates.
 * 
 * @author Rob Gordon
 */

public class Interval implements Serializable {
    private static final long serialVersionUID = 20050226;
    
    /** The end of time. */
    public static final long END_OF_TIME = Long.MAX_VALUE;
    
    /** The start of time. */
    public static final long START_OF_TIME = Long.MIN_VALUE;
    
    
	private final Date fromDate;
	private final Date toDate;
	
	
	public Interval(Date on) {
		this(on.getTime(), on.getTime());
	}
	
	/**
	 * Constructor for an interval between two dates.
	 * 
	 * @param from The starting date.
	 * @param to The ending date.
	 */
	public Interval(Date from, Date to) {
		this(from.getTime(), to.getTime());
	}
	
	
	/**
	 * Constructor for an interval between two dates given as milliseconds.
	 * 
	 * @param fromTime
	 * @param toTime
	 */
	public Interval(long fromTime, long toTime) {
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
	public Interval(Interval other) {
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
	public Date getToDate() {
		return toDate;
	}
	
	/**
	 * Crude implementation of hashCode, so intervals could
	 * be stored in HashSets.
	 * 
	 */
	public int hashCode() {
		return fromDate.hashCode() + toDate.hashCode();
	}

	/**
	 * Test if two interval are equivelent.
	 * <p>
	 * Intervals are equivelant if there start and end times are the same.
	 * 
	 * @param other The interval to test against.
	 * @return true if they are equal.
	 */
	public boolean equals(Object other) {
		if (!(other instanceof Interval)) {
			return false;
		}
		return this.toDate.equals(((Interval)other).toDate) 
				&& this.fromDate.equals(((Interval)other).fromDate);
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

	public boolean isBefore(Interval other) {
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
	
	public boolean isPast(Interval other) {
	    if (other == null) {
	        return true;
	    }
		return this.fromDate.getTime() > other.toDate.getTime();		
	}
			
	/**
	 * Provide an interval which is the result of this Limit 
	 * being limited by the given interval. This interval is the outer
	 * interval and the given interval is the refinement.
	 * <p>
	 * Results are determined as follows.
	 * <ul>
	 * <li>Simple Refinement - the given limit interval.</li>
	 * <li>Extended Refinement - the given limit interval.</li>
	 * <li>Eager Refinement - null.</li>
	 * <li>Anti refinement - null.</li>
	 * <li>Disjointed - null</li>
	 * </ul>
	 * 
	 * @param other The other interval. may be null.
	 * @return The new interval.
	 */

	public Interval limit(Interval limit) {
	    if (limit == null) {
	        return null;
	    }

	    if (limit.getFromDate().compareTo(this.getFromDate()) < 0) {
	    	return null;
	    }
	    
	    if (limit.getFromDate().compareTo(this.getToDate()) > 0) {
	    	return null;
	    }
	    
	    Date newStart;
	    if (this.getFromDate().compareTo(limit.getFromDate()) < 0) {
	    	newStart = limit.getFromDate();
	    }
	    else {
	    	newStart = this.getFromDate();
	    }
	    
	    return new Interval(newStart, limit.getToDate());
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
