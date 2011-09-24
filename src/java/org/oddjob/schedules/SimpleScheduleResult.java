package org.oddjob.schedules;

import java.io.Serializable;
import java.util.Date;

import org.oddjob.arooa.utils.DateHelper;

/**
 * A simple {@link ScheduleResult}
 * 
 * @author rob
 *
 */
public class SimpleScheduleResult implements ScheduleResult, Serializable {
	
	private static final long serialVersionUID = 2011091500L;
	
	private final Interval interval;
	
	private final Date useNext;
	
	public SimpleScheduleResult(Interval interval) {
		this(interval, interval.getToDate());
	}
	
	public SimpleScheduleResult(Interval interval, Date useNext) {
		if (interval == null) {
			throw new NullPointerException("No Interval.");
		}
		
		this.interval = new SimpleInterval(interval);
		this.useNext = useNext;
	}
	
	@Override
	public Date getFromDate() {
		return interval.getFromDate();
	}
	
	@Override
	public Date getToDate() {
		return interval.getToDate();
	}
	
	@Override
	public Date getUseNext() {
		return useNext;
	}
	
	/**
	 * Crude implementation of hashCode, so intervals could
	 * be stored in HashSets.
	 * 
	 */
	public int hashCode() {
		return interval.hashCode();
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
		if (other == null) {
			return false;
		}
		
		if (!(other instanceof ScheduleResult)) {
			return other.equals(this);
		}
		
		ScheduleResult result = (ScheduleResult) other;
		
		if (useNext == null) {
			if (result.getUseNext() != null) {
				return false;
			}
		}
		else {
			if (!useNext.equals(result.getUseNext())) {
				return false;
			}
		}
		
		return interval.getToDate().equals(result.getToDate()) 
			&& interval.getFromDate().equals(result.getFromDate());
	}
	
	@Override
	public String toString() {
		String useNextText = "";
		if (!interval.getToDate().equals(useNext)) {
			useNextText = ", use next " + 
			DateHelper.formatDateTimeInteligently(useNext);
		}
		
		return interval.toString() + useNextText;
	}
}
