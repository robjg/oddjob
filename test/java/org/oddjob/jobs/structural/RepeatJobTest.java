/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.jobs.structural;
import org.junit.Before;

import org.junit.Test;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.framework.StopWait;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.values.types.SequenceIterable;

import org.oddjob.OjTestCase;

/**
 *
 * @author Rob Gordon.
 */
public class RepeatJobTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(RepeatJobTest.class);
    
    volatile boolean stop;
    
	RepeatJob job;
    
   @Before
   public void setUp() {
		logger.debug("---------------- " + getName() 
		        + " ----------------");        
		
		stop = false;
		
		job = new RepeatJob();
		job.setName("Test Repeat");
	}
    
   @Test
    public void testSimpleRepeat3Times() {
    	    			
		Counter childJob = new Counter();
		job.setJob(childJob);
		
		job.setTimes(3);
		
		job.run();
		
		assertEquals("Test job should have run.", 3, childJob.count);
	}
	
   @Test
    public void testSimpleSequence() {
		
    	SequenceIterable seq = new SequenceIterable(1, 3, 1);
    	
		Counter childJob = new Counter();
		job.setJob(childJob);
		
		job.setValues(seq);
		
		job.run();
		
		assertEquals("Test job should have run.", 3, childJob.count);
	}
	
   @Test
    public void testSimpleUntil() {
		
		Runnable childJob = new SimpleJob() {
			
			@Override
			protected int execute() throws Throwable {
				job.setUntil(true);
				return 0;
			}
		};
		
		job.setJob(childJob);
				
		job.run();
		
		assertEquals(JobState.COMPLETE, 
				((Stateful) childJob).lastStateEvent().getState());
	}
    
   @Test
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
        	"      <echo id='echo'>Hello</echo>" +
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
        
        assertEquals("OJ complete", ParentState.COMPLETE, OddjobTestHelper.getJobState(oj));
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
	

	// the same simple count from oddjob;
   @Test
	public void testSimpleCountOJ() throws Exception {
		
		String xml = 
			"<oddjob xmlns:s='http://rgordon.co.uk/oddjob/schedules'>" +
			" <job>" +
			"  <repeat times='10'>" +
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

	public static class ExceptionJob implements Runnable {
		public void run() { 
			throw new RuntimeException("fail");
		}
	}
	
   @Test
	public void testSimpleFailOJ() throws Exception {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"    <repeat id='repeat'>" +
			"     <job>" +
			"      <bean id='c' class='" + ExceptionJob.class.getName() + "'/>" +
			"     </job>" +
			"    </repeat>" +
			" </job>" +
			"</oddjob>";

		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
		
		Stateful repeat = new OddjobLookup(oj).lookup(
				"repeat", Stateful.class);
		
		assertEquals(ParentState.EXCEPTION, repeat.lastStateEvent().getState());
		assertEquals(ParentState.EXCEPTION, oj.lastStateEvent().getState());
	}

   @Test
	public void testRepeatOnReady() {
		
		RepeatJob test = new RepeatJob();
		test.setTimes(3);
		
		test.setJob(new SequentialJob());
		
		test.run();
		
		assertEquals(3, test.getCount());
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
	}
	
   @Test
	public void testRepeatExample() {
		
		Oddjob oddjob = new Oddjob();
		
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/structural/RepeatExample.xml",
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.captureConsole()) {
			
			oddjob.run();
		}
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals("Hello 1", lines[0].trim());
		assertEquals("Hello 2", lines[1].trim());
		assertEquals("Hello 3", lines[2].trim());
		
		assertEquals(3, lines.length);
		
		oddjob.destroy();	
	}
	
   @Test
	public void testRepeatWithSequenceExample() {
		
		Oddjob oddjob = new Oddjob();
		
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/structural/RepeatWithSequence.xml",
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.captureConsole()) {
			
			oddjob.run();
		}
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());

		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals("Hello 1", lines[0].trim());
		assertEquals("Hello 2", lines[1].trim());
		assertEquals("Hello 3", lines[2].trim());
		
		assertEquals(3, lines.length);
		
		oddjob.destroy();	
	}
}


    
