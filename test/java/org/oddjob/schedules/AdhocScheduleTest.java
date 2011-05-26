package org.oddjob.schedules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.schedules.schedules.DayOfWeekSchedule;
import org.oddjob.schedules.schedules.TimeSchedule;

public class AdhocScheduleTest extends TestCase {

	private static final Logger logger = Logger.getLogger(AdhocScheduleTest.class);
	
	public void testForMeena() throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yy hh:mm");
		
		assertTrue(isIncluded(3, "10:00", "12:00", 
				format.parse("11-feb-09 11:00")));
		assertFalse(isIncluded(3, "10:00", "12:00", 
				format.parse("11-feb-09 09:59")));
		assertFalse(isIncluded(3, "10:00", "12:00",
				format.parse("11-feb-09 12:00")));

		// Boundaries not included.
		assertFalse(isIncluded(3, "10:00", "12:00", 
				format.parse("11-feb-09 10:00")));
		assertFalse(isIncluded(3, "10:00", "12:00",
				format.parse("12-feb-09 12:00")));
		
		// Over midnight examples
		assertTrue(isIncluded(3, "23:00", "01:00", 
				format.parse("11-feb-09 23:30")));
		assertFalse(isIncluded(3, "23:00", "01:00", 
				format.parse("11-feb-09 00:30")));
		assertTrue(isIncluded(3, "23:00", "01:00", 
				format.parse("12-feb-09 00:00")));
		assertFalse(isIncluded(3, "23:00", "01:00", 
				format.parse("11-feb-09 22:30")));
		assertFalse(isIncluded(3, "23:00", "01:00", 
				format.parse("12-feb-09 01:30")));
	}
	
	public static final boolean isIncluded (int day, 
			String startTime, String endTime, Date date) 
	throws ParseException {

		DayOfWeekSchedule dws = new DayOfWeekSchedule(); 

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
