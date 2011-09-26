/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.scheduling;

import java.text.ParseException;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.framework.StopWait;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.schedules.CountSchedule;
import org.oddjob.schedules.schedules.DailySchedule;
import org.oddjob.schedules.schedules.IntervalSchedule;
import org.oddjob.schedules.schedules.TimeSchedule;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.util.Clock;

/**
 *
 * @author Rob Gordon.
 */
public class RepeatJobTest extends TestCase {

    private static final Logger logger = Logger.getLogger(RepeatJobTest.class);
    volatile boolean stop;
    String iconId;
	RepeatJob job;
    
    public void setUp() {
		logger.debug("---------------- " + getName() 
		        + " ----------------");        
		stop = false;
		iconId = "none";
		job = new RepeatJob();
		job.setName("Test Repeat");
	}
    
    public void testInterval() 
	throws InterruptedException, ParseException, FailedToStopException {
		TimeSchedule schedule = new TimeSchedule();
		schedule.setAt("12:00");
    	
	    ManualClock clock = new ManualClock(); 	    
		clock.setDate("2003-12-25 11:59:58");
		
		job.addIconListener(new MyIconListener());
		job.setSchedule(schedule);		
		job.setClock(clock);
		CheckJob checkJob = new CheckJob();
		job.setJob(checkJob);
		
		Thread t = new Thread(job);
		logger.debug("> Starting Job execution thread.");
		t.start();
		// wait for repeat job to sleep
		while (!iconId.equals(IconHelper.SLEEPING)) {
		    Thread.sleep(100);
		}
		// first and only repeat.
		clock.setDate("2003-12-25 12:00:01");
		// wait for repeat job to sleep
		while (!stop) {
		    Thread.sleep(100);
		}
		job.stop();
		t.join();

		assertTrue("Test job should have run.", checkJob.hasRun);
	}
	
    public void testExceptionWithRetry()
	throws InterruptedException, ParseException, FailedToStopException {
		job.addIconListener(new MyIconListener());

		// set schedule
		DailySchedule schedule = new DailySchedule();
		schedule.setAt("12:00");
		
	    Clock clock = new Clock() {
	    	int i;
	    	Date[] dates = new Date[] { 
	    			DateHelper.parseDateTime("2003-12-25 11:59:58"),
	    			DateHelper.parseDateTime("2003-12-25 11:59:58"),
	    			DateHelper.parseDateTime("2003-12-25 12:00:01"),
	    			DateHelper.parseDateTime("2003-12-25 12:00:01"),
	    			DateHelper.parseDateTime("2003-12-25 12:00:03"),
	    			DateHelper.parseDateTime("2003-12-25 12:00:03"),
	    			DateHelper.parseDateTime("2003-12-25 12:00:10"),
	    			DateHelper.parseDateTime("2003-12-25 12:00:10"),
	    	};
	    	public Date getDate() {
	    		return dates[i++];
	    	}
	    };
		
		job.setSchedule(schedule);
		job.setRetry(retrySchedule());		
		job.setClock(clock);
		
		FlagState child = new FlagState();
		child.setState(JobState.INCOMPLETE);
		job.setJob(child)
		;
		CheckJob checkJob = new CheckJob();
		job.setException(checkJob);

		Thread t = new Thread(job);
		logger.debug("> Starting Job execution thread.");
		
		t.start();
		while (!stop) {
		    Thread.sleep(100);
		}		
	    job.stop();
		t.join();
		assertTrue("Test job should have run.", checkJob.hasRun);
    }

    public void testNoSchedules()
	throws InterruptedException, ParseException {
		
		CheckJob checkJob = new CheckJob();
		CheckJob exceptionJob = new CheckJob();
		job.setJob(checkJob);
		job.setException(exceptionJob);

		job.run();

		assertTrue("Test job should have run.", checkJob.hasRun);
		assertFalse(exceptionJob.hasRun);
    }
    
    public void testNoSchedulesException()
	throws InterruptedException, ParseException, FailedToStopException {
		
		FlagState child = new FlagState();
		child.setState(JobState.INCOMPLETE);
		job.setJob(child);
		
		CheckJob checkJob = new CheckJob();
		job.setException(checkJob);
		
		Thread t = new Thread(job);
		logger.debug("> Starting Job execution thread.");
		t.start();
		
		// job running will signal stop
		while (!stop) {
		    Thread.sleep(100);
		}		
	    job.stop();
		t.join();
		assertTrue("Test job should have run.", checkJob.hasRun);
    }
    
    public void testInOddjob() throws FailedToStopException {
        String config = 
        	"<oddjob xmlns:scheduling='http://rgordon.co.uk/oddjob/scheduling'>" +
        	" <job>" +
        	"  <sequential>" +
        	"   <jobs>" +
        	"    <scheduling:trigger on='${echo}' state='COMPLETE'>" +
        	"     <job>" +
        	"      <stop job='${whatever}'/>" +
        	"     </job>" +
        	"    </scheduling:trigger>" +
        	"    <repeat id='whatever'>" +
        	"     <job>" +
        	"      <echo id='echo' text='Hello'/>" +
        	"     </job>" +
            "    </repeat>" +
        	"   </jobs>" +
        	"  </sequential>" +
            " </job>" +
            "</oddjob>";
        
        Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", config));
        oj.run();
        
        new StopWait(oj).run();
        
        assertEquals("OJ complete", ParentState.COMPLETE, Helper.getJobState(oj));
    }
    
    private Schedule retrySchedule() throws ParseException {
		IntervalSchedule interval = new IntervalSchedule();
		interval.setInterval("00:00:02");
		
		CountSchedule count = new CountSchedule();
		count.setCount(1);
		count.setRefinement(interval);
		return count;
    }
    
    class CheckJob extends SimpleJob {

        boolean hasRun = false;
        
        @Override
        protected int execute() throws Throwable {
            hasRun = true;
            RepeatJobTest.this.stop = true;
        	return 0;
        }
    }
    
    class MyIconListener implements IconListener {
        public void iconEvent(IconEvent e) {
            iconId = e.getIconId();
        }
    }
    	
	public static class Counter extends SimpleJob {
		int count;
		
		@Override
		protected int execute() throws Throwable {
			count++; 			
			return 0;
		}
		
		public int getCount() {
			return count;
		}
	}
	
	// simple count test.
	public void testSimpleCount() {
		CountSchedule count = new CountSchedule();
		count.setCount(10);
		
		Counter counter = new Counter();
		
		RepeatJob rj = new RepeatJob();
		rj.setSchedule(count);
		rj.setJob(counter);
		
		rj.run();
		
		assertEquals(10, counter.count);
	}

	// the same simple count from oddjob;
	public void testSimpleCountOJ() throws Exception {
		
		String xml = 
			"<oddjob xmlns:s='http://rgordon.co.uk/oddjob/schedules'>" +
			" <job>" +
			"  <repeat>" +
			"   <schedule>" +
			"    <s:count count='10'/>" +
			"   </schedule>" +
			"   <job>" +
			"    <bean id='c' class='" + 
					Counter.class.getName() + "'/>" +
			"   </job>" +
			"  </repeat>" +
			" </job>" +
			"</oddjob>";

		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
		
		Object c = new OddjobLookup(oj).lookup("c");
		assertEquals(new Integer(10), PropertyUtils.getProperty(c, "count"));
	}

	public static class FailCounter implements Runnable {
		int count;
		public void run() { 
			count++; 
			throw new RuntimeException("fail");
		}
		public int getCount() {
			return count;
		}
	}
	
	// 2 normal and 2 * 3 retry = count of 8.
	public void testSimpleFailOJ() throws Exception {
		
		String xml = 
			"<oddjob xmlns:s='http://rgordon.co.uk/oddjob/schedules'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <folder>" +
			"     <jobs>" +
			"      <bean id='r' class='" + Counter.class.getName() + "'/>" +
			"     </jobs>" +
			"    </folder>" +
			"    <repeat exception='${r}'>" +
			"     <schedule>" +
			"      <s:count count='2'/>" +
			"     </schedule>" +
			"     <retry>" +
			"      <s:count count='3'/></retry>" +
			"     <job>" +
			"      <bean id='c' class='" + FailCounter.class.getName() + "'/>" +
			"     </job>" +
			"    </repeat>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";

		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
		
		Object fc = new OddjobLookup(oj).lookup("c");
		assertEquals(new Integer(8), PropertyUtils.getProperty(fc , "count"));
		Object r =  new OddjobLookup(oj).lookup("r");
		assertEquals(new Integer(2), PropertyUtils.getProperty(r , "count"));
	}

}


    