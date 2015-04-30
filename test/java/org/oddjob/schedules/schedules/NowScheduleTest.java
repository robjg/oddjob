package org.oddjob.schedules.schedules;

import java.text.ParseException;
import java.util.Date;

import junit.framework.TestCase;

import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;

public class NowScheduleTest extends TestCase {

	public void testNow() throws ParseException {
		
		NowSchedule test = new NowSchedule();
		
		Date testTime = DateHelper.parseDateTime("2011-09-16 12:00");
		
		ScheduleContext context = new ScheduleContext(testTime);
		
		ScheduleResult result = test.nextDue(context);
		
		assertEquals(testTime, result.getFromDate());
		assertEquals(testTime, result.getUseNext());
	}
	
	public void testNowWithLimits() throws ParseException {
		
		NowSchedule test = new NowSchedule();
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2011-09-16 12:00"));
		
		context = context.spawn(new IntervalTo(
				DateHelper.parseDate("2011-09-16"),
				DateHelper.parseDate("2011-09-17")));
		
		Interval result = test.nextDue(context);
		
		Interval expected = new IntervalTo(
				DateHelper.parseDateTime("2011-09-16 12:00"));
		
		context = context.spawn(new IntervalTo(
				DateHelper.parseDate("2011-09-17"),
				DateHelper.parseDate("2011-09-18")));
		
		result = test.nextDue(context);
		
		expected = null;
		
		assertEquals(expected, result);		
				
	}
}
