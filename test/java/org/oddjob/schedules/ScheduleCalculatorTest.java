/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.schedules;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.schedules.CountSchedule;
import org.oddjob.schedules.schedules.DateSchedule;
import org.oddjob.schedules.schedules.IntervalSchedule;
import org.oddjob.schedules.schedules.TimeSchedule;
import org.oddjob.scheduling.ManualClock;

/**
 *
 * @author Rob Gordon.
 */
public class ScheduleCalculatorTest extends TestCase {

    private static final Logger logger = Logger.getLogger(ScheduleCalculatorTest.class);

    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	TimeSchedule schedule;
	Schedule retrySchedule;
	
	String scheduleDate;
	String nextDue;
	Interval lastComplete;
	boolean failed;
	boolean retry;
	
	class SL implements ScheduleListener {
		/* (non-Javadoc)
		 * @see org.oddjob.treesched.ScheduleListener#initialised(java.util.Date)
		 */
		@Override
		public void initialised(Date scheduleDate) {
			ScheduleCalculatorTest.this.scheduleDate = 
				format.format(scheduleDate);
			ScheduleCalculatorTest.this.nextDue = 
				format.format(scheduleDate);
			ScheduleCalculatorTest.this.retry = false;
			ScheduleCalculatorTest.this.failed = false;
		}
		
		/* (non-Javadoc)
		 * @see org.oddjob.treesched.ScheduleListener#complete(java.util.Date, java.util.Date)
		 */
		@Override
		public void complete(Date scheduleDate, Interval lastComplete) {
			if (scheduleDate == null) {
				ScheduleCalculatorTest.this.scheduleDate = null; 
				ScheduleCalculatorTest.this.nextDue = null;				
			}
			else {
				ScheduleCalculatorTest.this.scheduleDate = 
					format.format(scheduleDate);
				ScheduleCalculatorTest.this.nextDue = 
					format.format(scheduleDate);
			}
			if (lastComplete == null) {
				ScheduleCalculatorTest.this.lastComplete = null;
			}
			else {
				ScheduleCalculatorTest.this.lastComplete = 
					lastComplete;				
			}
			ScheduleCalculatorTest.this.retry = false;
			ScheduleCalculatorTest.this.failed = false;
		}
		
		/* (non-Javadoc)
		 * @see org.oddjob.treesched.ScheduleListener#retry(java.util.Date, java.util.Date)
		 */
		public void retry(Date scheduleDate, Date retryDate) {
			ScheduleCalculatorTest.this.scheduleDate = 
				format.format(scheduleDate);
			ScheduleCalculatorTest.this.nextDue = 
				format.format(retryDate);
			ScheduleCalculatorTest.this.retry = true;
			ScheduleCalculatorTest.this.failed = false;
		}
		
		/* (non-Javadoc)
		 * @see org.oddjob.treesched.ScheduleListener#failed(java.util.Date)
		 */
		public void failed(Date scheduleDate) {
			ScheduleCalculatorTest.this.scheduleDate = 
				format.format(scheduleDate);
			ScheduleCalculatorTest.this.nextDue = 
				format.format(scheduleDate);
			ScheduleCalculatorTest.this.retry = false;
			ScheduleCalculatorTest.this.failed = true;
		}
	}
	
    public void setUp() throws ParseException {
		logger.debug("------------- " + getName() + " -------------");

		logger.debug("Default time zone: " + 
				TimeZone.getDefault().getDisplayName());
		
		// set schedule
		schedule = new TimeSchedule();
		schedule.setFrom("12:00");
		schedule.setTo("14:00");

        IntervalSchedule interval= new IntervalSchedule();
		interval.setInterval("00:10");
		CountSchedule count = new CountSchedule();
		count.setCount("2");
		count.setRefinement(interval);
		retrySchedule = count;
		
    }
    
    public void testInitialiseBeforeDue() throws Exception {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 11:00");

		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule);
		schedCalc.addScheduleListener(new SL());
    	schedCalc.initialise();
    	
		assertEquals("scheduleDate", "2003-12-25 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-25 12:00", nextDue);
		assertNull("lastComplete", lastComplete);
		assertFalse("retry", retry);
		assertFalse("failed", failed);
    }    
	
    public void testInitialiseDuringDue() throws Exception {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 12:10");

		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule);
		schedCalc.addScheduleListener(new SL());
    	schedCalc.initialise();
    	
		assertEquals("scheduleDate", "2003-12-25 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-25 12:00", nextDue);
		assertNull("lastComplete", lastComplete);
		assertFalse("retry", retry);
		assertFalse("failed", failed);
		
    }    

    public void testInitialiseAfterDue() throws Exception {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 14:10");

		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule);
		schedCalc.addScheduleListener(new SL());
    	schedCalc.initialise();
    	
		assertEquals("scheduleDate", "2003-12-26 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-26 12:00", nextDue);
		assertNull("lastComplete", lastComplete);
		assertFalse("retry", retry);
		assertFalse("failed", failed);
		
    }    
    
    public void testComplete() throws Exception {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 11:00");

		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule);
		schedCalc.addScheduleListener(new SL());
    	schedCalc.initialise();
    	schedCalc.calculateComplete();
    	
		assertEquals("scheduleDate", "2003-12-26 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-26 12:00", nextDue);
		assertEquals("lastComplete", 
				DateHelper.parseDateTime("2003-12-25 12:00"), 
				lastComplete.getFromDate());
		assertFalse("retry", retry);
		assertFalse("failed", failed);
		
	}
    
    public void testCompleteComplete() throws Exception {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 11:00");

		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule);
		schedCalc.addScheduleListener(new SL());
    	schedCalc.initialise();
    	schedCalc.calculateComplete();
    	schedCalc.calculateComplete();
    	
		assertEquals("scheduleDate", "2003-12-27 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-27 12:00", nextDue);
		assertEquals("lastComplete", 
				DateHelper.parseDateTime("2003-12-26 12:00"), 
				lastComplete.getFromDate());
		assertFalse("retry", retry);
		assertFalse("failed", failed);
		
	}
    
    public void testFailNoException() throws Exception {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 11:00");

		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule);
		schedCalc.addScheduleListener(new SL());
    	schedCalc.initialise();
    	schedCalc.calculateRetry();
    	
		assertEquals("scheduleDate", "2003-12-26 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-26 12:00", nextDue);
		assertNull("lastComplete", lastComplete);
		assertFalse("retry", retry);
		assertTrue("failed", failed);
		
	}
    
    public void testCompleteFailNoException() throws Exception {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 11:00");

		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule);
		schedCalc.addScheduleListener(new SL());
    	schedCalc.initialise();
    	schedCalc.calculateComplete();
    	schedCalc.calculateRetry();
    	
		assertEquals("scheduleDate", "2003-12-27 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-27 12:00", nextDue);
		assertEquals("lastComplete", 
				DateHelper.parseDateTime("2003-12-25 12:00"), 
				lastComplete.getFromDate());
		assertFalse("retry", retry);
		assertTrue("failed", failed);		
	}
    
    public void testExceptionScheduleOnce() throws ParseException {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 11:00");
    			
		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule, retrySchedule);
		schedCalc.addScheduleListener(new SL());

    	schedCalc.initialise();
    			
    	clock.setDate("2003-12-25 12:00");
    	schedCalc.calculateRetry();
    	
		assertEquals("scheduleDate", "2003-12-25 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-25 12:10", nextDue);
		assertNull("lastComplete", lastComplete);
		assertTrue("retry", retry);
		assertFalse("failed", failed);				
    }	
    
	public void testExceptionScheduleTwice() throws ParseException {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 11:00");
    	
		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule, retrySchedule);
		schedCalc.addScheduleListener(new SL());

    	schedCalc.initialise();
    	
		
    	clock.setDate("2003-12-25 12:00");
    	schedCalc.calculateRetry();
    	schedCalc.calculateRetry();
    	
		assertEquals("scheduleDate", "2003-12-25 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-25 12:20", nextDue);
		assertNull("lastComplete", lastComplete);
		assertTrue("retry", retry);
		assertFalse("failed", failed);				
	}

	// with a retry count of 2 this fails twice and then switches back
	// to the normal schedule.
	public void testExceptionScheduleThrice() throws ParseException {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 11:00");
    	
		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule, retrySchedule);
		schedCalc.addScheduleListener(new SL());

    	schedCalc.initialise();
    	
    	clock.setDate("2003-12-25 12:00");
    	schedCalc.calculateRetry();
    	schedCalc.calculateRetry();
    	schedCalc.calculateRetry();
    	
		assertEquals("scheduleDate", "2003-12-26 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-26 12:00", nextDue);
		assertNull("lastComplete", lastComplete);
		assertFalse("retry", retry);
		assertTrue("failed", failed);				
    
    }
	
	// with a retry count of 2 this fails twice and then switches back
	// to the normal schedule, then back to the retry.
	public void testExceptionScheduleFourth() throws ParseException {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 11:00");
    	
		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule, retrySchedule);
		schedCalc.addScheduleListener(new SL());

    	schedCalc.initialise();
    	
		
    	schedCalc.calculateRetry();
    	schedCalc.calculateRetry();
    	schedCalc.calculateRetry();
    	clock.setDate("2003-12-26 12:00");
    	schedCalc.calculateRetry();
    	
		assertEquals("scheduleDate", "2003-12-26 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-26 12:10", nextDue);
		assertNull("lastComplete", lastComplete);
		assertTrue("retry", retry);
		assertFalse("failed", failed);				
    
    }
	
    /**
     * Test what happens when a schedule completes
     * all together.
     */
    public void testCompletes()
	throws InterruptedException, ParseException {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 11:00");
    	
		// set schedule
		DateSchedule dateSchedule = new DateSchedule();
		dateSchedule.setOn("2003-12-25");
		TimeSchedule timeSchedule = new TimeSchedule();
		timeSchedule.setAt("12:00");
		
		dateSchedule.setRefinement(timeSchedule);
		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, dateSchedule);
		schedCalc.addScheduleListener(new SL());

    	schedCalc.initialise();
    	
    	schedCalc.calculateComplete();
    	
		assertNull("scheduleDate", scheduleDate);
		assertNull("nextDue", nextDue);
		assertEquals("lastComplete", 
				DateHelper.parseDateTime("2003-12-25 12:00"), 
				lastComplete.getFromDate());
		assertFalse("retry", retry);
		assertFalse("failed", failed);
		
    }
        
    /** 
     * Test as if states been persited by setting lastComplete.
     */
    public void testPersitence() 
    throws Exception {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 11:00");

		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule);
		schedCalc.addScheduleListener(new SL());

		Map<Object, Object> m = new HashMap<Object, Object>();
    	schedCalc.initialise(null, m);

    	schedCalc.calculateComplete();

    	schedCalc = new ScheduleCalculator(clock, schedule);
    	schedCalc.initialise(lastComplete, m);
    	
		assertEquals("scheduleDate", "2003-12-26 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-26 12:00", nextDue);
		assertFalse("retry", retry);
		assertFalse("failed", failed);
    }

    public void testPersistence2() throws ParseException {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 11:00");
    	
		
		CountSchedule countSchedule = new CountSchedule();
		countSchedule.setCount("1");
		DateSchedule dateSchedule = new DateSchedule();
		dateSchedule.setOn("2020-12-25");
		
		ScheduleList scheduleList = new ScheduleList();
		scheduleList.setSchedules(new Schedule[] { 
						countSchedule, dateSchedule });
		
		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, scheduleList);
		schedCalc.addScheduleListener(new SL());
		
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		
		schedCalc.initialise(null, map);
				
		schedCalc = new ScheduleCalculator(clock, scheduleList);
		schedCalc.addScheduleListener(new SL());
		
		schedCalc.initialise(
				new IntervalTo(
						format.parse("2003-12-25 12:00"), 
						format.parse("2003-12-25 12:10")), 
				map);
		
		assertEquals("2020-12-25 00:00", nextDue);
		
    }
    
	// test the retry is limited by the main schedule
	public void testLimitedRetry() throws ParseException {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 13:55");
    	
		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule, retrySchedule);
		schedCalc.addScheduleListener(new SL());

    	schedCalc.initialise();
    			
		assertEquals("scheduleDate", "2003-12-25 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-25 12:00", nextDue);
		assertNull("lastComplete", lastComplete);
		assertFalse("retry", retry);
		assertFalse("failed", failed);
		
    	schedCalc.calculateRetry();
    	
		assertEquals("scheduleDate", "2003-12-25 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-25 14:00", nextDue);
		assertNull("lastComplete", lastComplete);
		assertTrue("retry", retry);
		assertFalse("failed", failed);
		
    	schedCalc.calculateRetry();
    	
		assertEquals("scheduleDate", "2003-12-26 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-26 12:00", nextDue);
		assertNull("lastComplete", lastComplete);
		assertFalse("retry", retry);
		assertTrue("failed", failed);				
    
    }
	
	// test behaviour of a retry after the next due interval.
	public void testRetryAfterInterval() throws ParseException {
    	ManualClock clock = new ManualClock();
    	clock.setDate("2003-12-25 14:05");
    	
		ScheduleCalculator schedCalc = new ScheduleCalculator(clock, schedule, retrySchedule);
		schedCalc.addScheduleListener(new SL());

		IntervalTo lastInterval = new IntervalTo(
				format.parse("2003-12-24 12:00"),
				format.parse("2003-12-24 14:00"));
		
		schedCalc.initialise(lastInterval, new HashMap<Object, Object>());
    			
		assertEquals("scheduleDate", "2003-12-25 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-25 12:00", nextDue);
		assertNull("lastComplete", lastComplete);
		assertFalse("retry", retry);
		assertFalse("failed", failed);
		
    	schedCalc.calculateRetry();
    	
		assertEquals("scheduleDate", "2003-12-26 12:00", scheduleDate);
		assertEquals("nextDue", "2003-12-26 12:00", nextDue);
		assertNull("lastComplete", lastComplete);
		assertFalse("retry", retry);
		assertTrue("failed", failed);				
    
    }
}
