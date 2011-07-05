/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.oddjob.Helper;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;

/**
 * 
 */
public class CountScheduleTest extends TestCase {
	
	public void testCount() {
		class Counter implements Schedule {
			int count;
			public IntervalTo nextDue(ScheduleContext context) {
				count++;
				return new IntervalTo(new Date(0));
			}
		}
		CountSchedule test = new CountSchedule();
		test.setCount("3");
		Counter c = new Counter();
		test.setRefinement(c);
		IntervalTo nextDue = null;
		ScheduleContext context = new ScheduleContext(new Date());
		do {
			context = context.move(
					new Date(context.getDate().getTime() + 1));
			nextDue = test.nextDue(context);
		} while (nextDue != null);
		
		assertEquals(3, c.count);
	}
	
	public void testHowManyNextDues() {

		int count = 0;
		
		CountSchedule test = new CountSchedule(3);
		
		ScheduleContext context = new ScheduleContext(new Date());
		
		while (test.nextDue(context) != null) {
			context = context.move(new Date(
					context.getDate().getTime() + 1));
			++count;
		}
		
		assertEquals(3, count);
	}
	
	/**
	 * Test that a CountSchedule can be serialized.
	 * 
	 * @throws Exception
	 */
	public void testSerialize() throws Exception {
		CountSchedule test = new CountSchedule();
		test.setCount("1");

		Date now = new Date();
		
		Map<Object, Object> map = new HashMap<Object, Object>();
		ScheduleContext context = new ScheduleContext(
				now, null, map);
		
		IntervalTo interval = test.nextDue(context);
		assertNotNull(interval);
		
		Schedule copy = (Schedule) Helper.copy(test);
		
		context = new ScheduleContext(
				new Date(now.getTime() + 1), null, map);
		
		interval = copy.nextDue(context);
		assertEquals(null, interval);
		
	}
	
	public void testLimitedByParentInterval() throws ParseException {
		
		IntervalTo parentInterval = new IntervalTo(
				DateHelper.parseDateTime("2010-06-27 07:00"),
				DateHelper.parseDateTime("2010-06-27 10:00"));
		
		CountSchedule test = new CountSchedule(3);

		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2010-06-27 09:00"));
		
		context = context.spawn(parentInterval);
		
		IntervalTo interval1 = test.nextDue(context);
		
		assertEquals(new IntervalTo(
				DateHelper.parseDateTime("2010-06-27 09:00")),
				interval1);
		
		context = context.spawn(DateHelper.parseDateTime("2010-06-27 11:00"),
				parentInterval);
		
		IntervalTo interval2 = test.nextDue(context);
		
		assertNull(interval2);
	}
}
