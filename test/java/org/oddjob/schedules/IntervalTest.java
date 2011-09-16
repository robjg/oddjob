package org.oddjob.schedules;


import java.text.ParseException;

import junit.framework.TestCase;

import org.oddjob.arooa.utils.DateHelper;

/**
 * 
 */
public class IntervalTest extends TestCase {

	public IntervalTest(String arg0) {
		super(arg0);
	}

	/*
	 * Test for boolean equals(BetweenTimes)
	 */
	public void testEquals() throws ParseException {

		Interval test = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:15"), 
				DateHelper.parseDateTime("2003-07-12 23:17"));

		Interval copy = new IntervalTo(test);
		
		assertTrue(test.equals(copy));
	}

	/*
	 * Test for boolean isBefore(BetweenTimes)
	 */
	public void testIsBeforeBetweenTimes() throws ParseException {

		IntervalBase test1 = new IntervalTo(
		        DateHelper.parseDateTime("2003-07-11 12:15"), 
		        DateHelper.parseDateTime("2003-07-12 23:17"));

		IntervalBase test2 = new IntervalTo(
		        DateHelper.parseDateTime("2003-07-12 12:15"), 
		        DateHelper.parseDateTime("2003-07-13 23:17"));
				
		assertTrue(test1.isBefore(test2));
	}

	/*
	 * Test for boolean isPast(BetweenTimes)
	 */
	public void testIsPastBetweenTimes() throws ParseException {

		IntervalBase test1 = new IntervalBase(
				DateHelper.parseDateTime("2003-07-11 12:15"), 
				DateHelper.parseDateTime("2003-07-12 23:17"));

		IntervalBase test2 = new IntervalBase(
		        DateHelper.parseDateTime("2003-07-13 12:15"), 
		        DateHelper.parseDateTime("2003-07-13 23:17"));
				
		assertTrue(test2.isPast(test1));
	}

	public void testLimitSimpleRefinement() throws ParseException {
		Interval result;
		
		Interval i1 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:15"),
				DateHelper.parseDateTime("2003-07-11 13:15"));
		
		Interval i2 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:30"), 
				DateHelper.parseDateTime("2003-07-11 13:00"));
		
		result = new IntervalHelper(i1).limit(i2);
		
		assertEquals(i2, result);
		
	}
	
	public void testLimitExtendedRefinement() throws ParseException {
		Interval result;
		
		Interval i1 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:15"),
				DateHelper.parseDateTime("2003-07-11 13:15"));
		
		Interval i2 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:30"), 
				DateHelper.parseDateTime("2003-07-11 13:30"));
		
		Interval expected = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:30"), 
				DateHelper.parseDateTime("2003-07-11 13:30"));
		
		result = new IntervalHelper(i1).limit(i2);
		assertEquals(expected, result);
		
	}
	
	public void testLimitEagerRefinement() throws ParseException {
		Interval result;
		
		Interval i1 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:15"),
				DateHelper.parseDateTime("2003-07-11 13:15"));
		
		Interval i2 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:00"), 
				DateHelper.parseDateTime("3003-07-11 13:00"));
		
		result = new IntervalHelper(i1).limit(i2);
		
		assertNull(result);
	}
	
	public void testLimitAntiRefinement() throws ParseException {
		Interval result;
		
		Interval i1 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:15"),
				DateHelper.parseDateTime("2003-07-11 13:15"));
		
		Interval i2 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:00"), 
				DateHelper.parseDateTime("2003-07-11 13:30"));
		
		result = new IntervalHelper(i1).limit(i2);
		
		assertNull(result);
	}
	
	public void testLimitDisjointedAfter() throws ParseException {
		Interval result;
		
		Interval i1 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:15"),
				DateHelper.parseDateTime("2003-07-11 13:15"));
		
		Interval i2 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 14:00"), 
				DateHelper.parseDateTime("2003-07-11 15:30"));
		
		result = new IntervalHelper(i1).limit(i2);
		assertNull(result);
	}
	
	public void testLimitDisjointedBefore() throws ParseException {
		Interval result;
		
		Interval i1 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:15"),
				DateHelper.parseDateTime("2003-07-11 13:15"));
		
		Interval i2 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 10:00"), 
				DateHelper.parseDateTime("2003-07-11 11:30"));
		
		result = new IntervalHelper(i1).limit(i2);
		assertNull(result);
	}
	
	public void testLimitNull() throws ParseException {
		
		Interval i1 = new IntervalTo(
				DateHelper.parseDateTime("2003-07-11 12:15"),
				DateHelper.parseDateTime("2003-07-11 13:15"));
		
		// This is an important assumption....
		assertNull(new IntervalHelper(i1).limit(null));
	}
}
