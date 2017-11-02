package org.oddjob.schedules.regression;
import org.junit.After;

import org.junit.Test;

import java.util.TimeZone;

import org.oddjob.OjTestCase;

/**
 * 
 */
public class ScheduleAllTest extends OjTestCase {

    @After
    public void tearDown() throws Exception {
		TimeZone.setDefault(null);
	}
	
   @Test
	public void testDaily() throws Exception {
	
		new ScheduleTester("daily_schedule_test.xml").run();

		TimeZone tz = TimeZone.getTimeZone("Syndney/Australia");
		
		new ScheduleTester("daily_schedule_test.xml", tz).run();
		
		tz = TimeZone.getTimeZone("America/New_York");

		new ScheduleTester("daily_schedule_test.xml", tz).run();
	}
	
   @Test
	public void testMonthly() throws Exception {
		
		new ScheduleTester("monthly_schedule_test.xml").run();
		
		TimeZone tz = TimeZone.getTimeZone("Syndney/Australia");
		
		new ScheduleTester("monthly_schedule_test.xml", tz).run();
		
		tz = TimeZone.getTimeZone("America/New_York");

		new ScheduleTester("monthly_schedule_test.xml", tz).run();
	}
	
   @Test
	public void testBroken() throws Exception {

		new ScheduleTester("broken_schedule_test.xml").run();

		TimeZone tz = TimeZone.getTimeZone("Syndney/Australia");
		
		new ScheduleTester("broken_schedule_test.xml", tz).run();
		
		tz = TimeZone.getTimeZone("America/New_York");

		new ScheduleTester("broken_schedule_test.xml", tz).run();
	}
	

   @Test
	public void testWeekly() throws Exception {
		
		new ScheduleTester("weekly_schedule_test.xml").run();

		TimeZone tz = TimeZone.getTimeZone("Syndney/Australia");
		
		new ScheduleTester("weekly_schedule_test.xml", tz).run();
		
		tz = TimeZone.getTimeZone("America/New_York");
		
		new ScheduleTester("weekly_schedule_test.xml", tz).run();
	}
	
   @Test
	public void testYearly() throws Exception {
		
		new ScheduleTester("yearly_schedule_test.xml").run();
		
		TimeZone tz = TimeZone.getTimeZone("Syndney/Australia");
		
		new ScheduleTester("yearly_schedule_test.xml", tz).run();
		
		tz = TimeZone.getTimeZone("America/New_York");
		
		new ScheduleTester("yearly_schedule_test.xml", tz).run();		
	}
}
