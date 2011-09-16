package org.oddjob.schedules;

public class IntervalHelper {

	private final IntervalTo interval;
	
	public IntervalHelper(Interval interval) {
		this.interval = new IntervalTo(interval);
	}

	public boolean isBefore(Interval other) {
		return interval.isBefore(
				new IntervalTo(other));
	}
	
	public Interval limit(Interval other) {
		return interval.limit(other);
	}
	
	public boolean isPoint() {
		return interval.isPoint();
	}
}
