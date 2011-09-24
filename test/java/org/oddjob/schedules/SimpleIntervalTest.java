package org.oddjob.schedules;

import java.text.ParseException;
import java.util.Date;

import junit.framework.TestCase;

import org.oddjob.arooa.utils.DateHelper;

public class SimpleIntervalTest extends TestCase {

	
	public void testOn() throws ParseException {

		Date date = DateHelper.parseDateTime("2006-03-02 10:00");
		
		SimpleInterval test  = new SimpleInterval(
					date); 
		
		assertEquals(date, test.getFromDate()); 
		assertEquals(DateUtils.oneMillisAfter(date), test.getToDate()); 		
	}
	
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
