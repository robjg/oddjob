package org.oddjob.schedules;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.schedules.schedules.WeeklySchedule;
import org.oddjob.schedules.schedules.TimeSchedule;
import org.oddjob.schedules.units.DayOfWeek;

public class AdhocScheduleTest extends OjTestCase {

	private static final Logger logger = Logger.getLogger(AdhocScheduleTest.class);
	
   @Test
	public void testForMeena() throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yy hh:mm");
		
		assertTrue(isIncluded(DayOfWeek.Days.WEDNESDAY, 
				"10:00", "12:00", 
				format.parse("11-feb-09 11:00")));
		assertFalse(isIncluded(DayOfWeek.Days.WEDNESDAY, "10:00", "12:00", 
				format.parse("11-feb-09 09:59")));
		assertFalse(isIncluded(DayOfWeek.Days.WEDNESDAY, "10:00", "12:00",
				format.parse("11-feb-09 12:00")));

		// Boundaries not included.
		assertFalse(isIncluded(DayOfWeek.Days.WEDNESDAY, "10:00", "12:00", 
				format.parse("11-feb-09 10:00")));
		assertFalse(isIncluded(DayOfWeek.Days.WEDNESDAY, "10:00", "12:00",
				format.parse("12-feb-09 12:00")));
		
		// Over midnight examples
		assertTrue(isIncluded(DayOfWeek.Days.WEDNESDAY, "23:00", "01:00", 
				format.parse("11-feb-09 23:30")));
		assertFalse(isIncluded(DayOfWeek.Days.WEDNESDAY, "23:00", "01:00", 
				format.parse("11-feb-09 00:30")));
		assertTrue(isIncluded(DayOfWeek.Days.WEDNESDAY, "23:00", "01:00", 
				format.parse("12-feb-09 00:00")));
		assertFalse(isIncluded(DayOfWeek.Days.WEDNESDAY, "23:00", "01:00", 
				format.parse("11-feb-09 22:30")));
		assertFalse(isIncluded(DayOfWeek.Days.WEDNESDAY, "23:00", "01:00", 
				format.parse("12-feb-09 01:30")));
	}
	
	public static final boolean isIncluded (DayOfWeek day, 
			String startTime, String endTime, Date date) 
	throws ParseException {

		WeeklySchedule dws = new WeeklySchedule(); 

		dws.setOn(day);

		TimeSchedule time = new TimeSchedule();

		time.setFrom(startTime);

		time.setTo(endTime);

		dws.setRefinement(time);

		Interval result = dws.nextDue(new ScheduleContext(date));

		logger.debug("Day=" + day + ", from=" + startTime + 
				", to=" + endTime + ", date=" + date +
				", result=" + result);
		
		return date.after(result.getFromDate()) 
			&& date.before(result.getToDate()); 
	}
}
