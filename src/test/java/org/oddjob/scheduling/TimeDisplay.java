package org.oddjob.scheduling;

import java.text.DecimalFormat;

public class TimeDisplay {

	private final int days;
	private final int hours;
	private final int minutes;
	private final int seconds;
	private final int milliseconds;
	
	public TimeDisplay(long time) {
		
		milliseconds = (int) (time % 1000);

		long remainder = time / 1000;

		seconds = (int) remainder % 60;

		remainder = remainder / 60;
		
		minutes = (int) remainder % 60;

		remainder = remainder / 60;
		
		hours = (int) remainder % 24;

		days = (int) remainder / 24;
	}

	public int getDays() {
		return days;
	}

	public int getHours() {
		return hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getSeconds() {
		return seconds;
	}

	public int getMilliseconds() {
		return milliseconds;
	}
	
	public String toString() {
			
		return new DecimalFormat("#,##0").format(days) + " " +
				new DecimalFormat("00").format(hours) + ":" +
				new DecimalFormat("00").format(minutes) + ":" +
				new DecimalFormat("00").format(seconds) + ":" +
				new DecimalFormat("000").format(milliseconds);
	}
}
