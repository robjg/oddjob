package org.oddjob.schedules;

import java.io.Serializable;
import java.util.Date;

import org.oddjob.arooa.utils.DateHelper;

/**
 * A Simple implementation of an {@link Interval).
 * 
 * @author rob
 *
 */
public class SimpleInterval implements Interval, Serializable {

	private static final long serialVersionUID = 2011092300L;
	
	private final Date fromDate;
	private final Date toDate;
		
	/**
	 * 
	 * @param on
	 */
	public SimpleInterval(Date on) {
		this(on.getTime(), on.getTime() + 1);
	}
	
	/**
	 * Constructor for an interval between two dates.
	 * 
	 * @param from The starting date.
	 * @param to The ending date.
	 */
	public SimpleInterval(Date from, Date to) {
		this(from.getTime(), to.getTime());
	}
	
	
	/**
	 * Constructor for an interval between two dates given as milliseconds.
	 * 
	 * @param fromTime
	 * @param toTime
	 */
	public SimpleInterval(long fromTime, long toTime) {
		fromDate = new Date(fromTime);
		toDate = new Date(toTime);			
		
		if (toTime <= fromTime) {
			throw new IllegalStateException("Interval " + this + 
					" must have a from date before the to date");
		}
	}

	/**
	 * The copy constructor.
	 *  
	 * @param other The other interval.
	 */
	public SimpleInterval(Interval other) {
		this(other.getFromDate().getTime(), other.getToDate().getTime());
	}

	
	@Override
	public Date getFromDate() {
		return fromDate;
	}
	
	@Override
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
	 * Test if two intervals are equivalent.
	 * <p>
	 * Intervals are equivalent if there start and end times 
	 * are the same.
	 * 
	 * @param other The interval to test against.
	 * @return true if they are equal.
	 */
	public boolean equals(Object other) {
		if (!(other instanceof Interval)) {
			return false;
		}
		
		Interval interval = (Interval) other;
			
		return this.toDate.equals(interval.getToDate()) 
					&& this.fromDate.equals(interval.getFromDate());
	}
	
	/**
	 * Return a string representation of this interval.
	 */		
	public String toString() {

		if (toDate.getTime() - fromDate.getTime() == 1) {
			return "at " + 
				DateHelper.formatDateTimeInteligently(getFromDate());
		}
		else {
			return DateHelper.formatDateTimeInteligently(getFromDate()) + 
				" up to " + 
				DateHelper.formatDateTimeInteligently(getToDate());	
		}		
	}
	
}
