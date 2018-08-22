package org.oddjob.schedules;

import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.utils.DateHelper;

public class SimpleIntervalTest extends OjTestCase {

	
   @Test
	public void testOn() throws ParseException {

		Date date = DateHelper.parseDateTime("2006-03-02 10:00");
		
		SimpleInterval test  = new SimpleInterval(
					date); 
		
		assertEquals(date, test.getFromDate()); 
		assertEquals(DateUtils.oneMillisAfter(date), test.getToDate()); 		
	}
	
   @Test
	public void testEquals() throws ParseException {
		
		Interval i1, i2;
		
		i1 = new SimpleInterval(
					DateHelper.parseDateTime("2006-03-02 10:00"),
					DateHelper.parseDateTime("2006-03-02 11:00")); 
		
		i2 = new SimpleInterval(
					DateHelper.parseDateTime("2006-03-02 10:00"),
					DateHelper.parseDateTime("2006-03-02 11:00")); 
		
		assertEquals(i1, i2);
		
		i1 = new SimpleInterval(i2); 
				
		assertEquals(i1, i2);
		
	}
	
   @Test
	public void testNotEquals() throws ParseException {
		
		Interval i1, i2;
		
		i1 = new SimpleInterval(
						DateHelper.parseDateTime("2003-06-02 10:00"),
						DateHelper.parseDateTime("2003-06-02 17:00"));
		
		i2 = new SimpleInterval(
						DateHelper.parseDateTime("2003-06-02 15:00"),
						DateHelper.parseDateTime("2003-06-02 20:00"));		
		
		assertFalse(i1.equals(i2));
	}
}
