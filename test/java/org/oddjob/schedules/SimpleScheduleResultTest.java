package org.oddjob.schedules;

import java.text.ParseException;

import org.oddjob.arooa.utils.DateHelper;

import junit.framework.TestCase;

public class SimpleScheduleResultTest extends TestCase {

	public void testEquals() throws ParseException {
	
		Object o1, o2;
		
		o1 = new SimpleScheduleResult(
				new IntervalTo(
					DateHelper.parseDateTime("2006-03-02 10:00"),
					DateHelper.parseDateTime("2006-03-02 11:00"))); 
		
		o2 = new SimpleScheduleResult(
				new IntervalTo(
					DateHelper.parseDateTime("2006-03-02 10:00"),
					DateHelper.parseDateTime("2006-03-02 11:00"))); 
		
		assertEquals(o1, o2);
		
		o1 = new SimpleScheduleResult(
				new IntervalTo(
					DateHelper.parseDateTime("2006-03-02 10:00"),
					DateHelper.parseDateTime("2006-03-02 11:00")),
				null); 
		
		o2 = new SimpleScheduleResult(
				new IntervalTo(
					DateHelper.parseDateTime("2006-03-02 10:00"),
					DateHelper.parseDateTime("2006-03-02 11:00")),
				null); 
		
		assertEquals(o1, o2);
		
	}
	
	public void testNotEquals() throws ParseException {
		
		Object o1, o2;
		
		o1 = new SimpleScheduleResult(
				new SimpleInterval(
						DateHelper.parseDateTime("2003-06-02 10:00"),
						DateHelper.parseDateTime("2003-06-02 17:00")),
				DateHelper.parseDate("2003-06-03"));		
		
		o2 = new SimpleScheduleResult(
				new SimpleInterval(
						DateHelper.parseDateTime("2003-06-02 15:00"),
						DateHelper.parseDateTime("2003-06-02 20:00")),
				DateHelper.parseDate("2003-06-03"));		
		
		assertFalse(o1.equals(o2));
		
		o1 = new SimpleScheduleResult(
				new SimpleInterval(
						DateHelper.parseDateTime("2003-06-02 10:00"),
						DateHelper.parseDateTime("2003-06-02 17:00")),
				null);		
		
		o2 = new SimpleScheduleResult(
				new SimpleInterval(
						DateHelper.parseDateTime("2003-06-02 10:00"),
						DateHelper.parseDateTime("2003-06-02 17:00")),
				DateHelper.parseDate("2003-06-03"));		
		
		assertFalse(o1.equals(o2));
	}
}
