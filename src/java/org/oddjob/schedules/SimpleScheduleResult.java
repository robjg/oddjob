package org.oddjob.schedules;

import java.io.Serializable;
import java.util.Date;

public class SimpleScheduleResult implements ScheduleResult, Serializable {
	
	private static final long serialVersionUID = 2011091500L;
	
	private final Interval interval;
	
	private final Date useNext;
	
	public SimpleScheduleResult(Interval interval) {
		this(interval, interval.getToDate());
	}
	
	public SimpleScheduleResult(Interval interval, Date useNext) {
		this.interval = interval;
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
		return interval.hashCode() + useNext.hashCode();
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
		
		ScheduleResult interval = (ScheduleResult) other;
		
		return this.interval.equals(other)
				&& this.getUseNext().equals(interval.getUseNext());
	}
	
	@Override
	public String toString() {
		return interval.toString();
	}
}
