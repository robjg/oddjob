package org.oddjob.schedules;

import java.util.Date;

import org.oddjob.arooa.utils.DateHelper;

/**
 * An Interval that extends to, but does not include the to date.
 * 
 * @author rob
 *
 */
public class IntervalTo extends IntervalBase implements ScheduleResult {
	
	private static final long serialVersionUID = 2009022700L;

	/**
	 * Create a point interval.
	 * 
	 * @param on
	 */
	public IntervalTo(Date on) {
		super(on);
	}
	
	/**
	 * Create an interval up to the to Date. The upTo date
	 * must be at least a millisecond greater than the from date.
	 * If they are the same then use the single date constructor.
	 * 
	 * @param from
	 * @param upTo
	 */
	public IntervalTo(Date from, Date upTo) {
		super(from.getTime(), upTo.getTime() - 1);
	}

	/**
	 * Create an copy of the given Interval.
	 * 
	 * @param interval
	 */
	public IntervalTo(Interval interval) {
		super(interval.getFromDate().getTime(), 
				interval.getToDate().getTime() - 1);
	}
	
	
	/**
	 * Get the upTo date which is a millisecond after the 
	 * inclusive to date.
	 * 
	 * @return
	 */
	public Date getToDate() {
		return new Date(getEndDate().getTime() + 1);
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
	    
	    if (limit.getFromDate().compareTo(this.getEndDate()) > 0) {
	    	return null;
	    }
	    
	    Date newStart;
	    if (this.getFromDate().compareTo(limit.getFromDate()) < 0) {
	    	newStart = limit.getFromDate();
	    }
	    else {
	    	newStart = this.getFromDate();
	    }
	    
	    return new IntervalTo(newStart, limit.getToDate());
	}
	
	/**
	 * Crude implementation of hashCode, so intervals could
	 * be stored in HashSets.
	 * 
	 */
	public int hashCode() {
		return getFromDate().hashCode() + getToDate().hashCode();
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
		
		if (other instanceof ScheduleResult
				&& !getUseNext().equals(
						((ScheduleResult) other).getUseNext())) {
			return false;
		}
		else {
			Interval interval = (Interval) other;
			
			return this.getToDate().equals(interval.getToDate()) 
					&& this.getFromDate().equals(interval.getFromDate());
		}
	}
	
	/**
	 * Return a string representation of this interval.
	 */		
	public String toString() {

		if (getFromDate().equals(getEndDate())) {
			return "at " + 
				DateHelper.formatDateTimeInteligently(getFromDate());
		}
		else {
			return DateHelper.formatDateTimeInteligently(getFromDate()) + 
				" up to " + 
				DateHelper.formatDateTimeInteligently(getToDate());	
		}		
	}
	
	@Override
	public Date getUseNext() {
		return getToDate();
	}
}
