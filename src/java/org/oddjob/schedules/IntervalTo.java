package org.oddjob.schedules;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.oddjob.arooa.ArooaConstants;

/**
 * An Interval that extends to, but does not include the to date.
 * 
 * @author rob
 *
 */
public class IntervalTo extends Interval {
	private static final long serialVersionUID = 2009022700L;

	private static final String DATE_FORMAT_SECONDS =
		ArooaConstants.DATE_FORMAT + " " + ArooaConstants.TIME_FORMAT2;
	
	private static final String DATE_FORMAT_MILLISECONDS =
		ArooaConstants.DATE_FORMAT + " " + ArooaConstants.TIME_FORMAT1;
	
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
		super(interval.getFromDate(), interval.getToDate());
	}
	
	
	/**
	 * Get the upTo date which is a millisecond after the 
	 * inclusive to date.
	 * 
	 * @return
	 */
	public Date getUpToDate() {
		return new Date(getToDate().getTime() + 1);
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

	public IntervalTo limit(Interval limit) {
		
		Interval result = super.limit(limit);
		
	    if (result == null) {
	        return null;
	    }
	    
	    return new IntervalTo(result);
	}
	
	/**
	 * Return a string representation of this interval.
	 */		
	public String toString() {

		if (getFromDate().equals(getToDate())) {
			return "at " + formatDate(getFromDate());
		}
		else {
			return formatDate(getFromDate())+ " up to " + 
					formatDate(getUpToDate());	
		}
	}

	private String formatDate(Date date) {
		if (date.getTime() % 1000 == 0) {
			// no milliseconds - then miss them off.
			return new SimpleDateFormat(
					DATE_FORMAT_SECONDS).format(date);
		}
		else {
			return new SimpleDateFormat(
					DATE_FORMAT_MILLISECONDS).format(date);
		}
		
	}
}
