package org.oddjob.jobs.job;
import org.junit.Before;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;

public class ResetJobTest extends OjTestCase {

	private static final Logger logger = Logger.getLogger(
			ResetJobTest .class);
	
   @Before
   public void setUp() throws Exception {

		logger.info("-------------------------  " + getName() + 
				"  ------------------------");
	}
	
   @Test
	public void testReset() {
		
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		job.run();
		
		assertEquals(JobState.INCOMPLETE, job.lastStateEvent().getState());
		
		ResetJob test = new ResetJob();
		
		test.setJob(job);
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		assertEquals(JobState.READY, job.lastStateEvent().getState());
		
		job.setState(JobState.COMPLETE);
		
		job.run();
		
		assertEquals(JobState.COMPLETE, job.lastStateEvent().getState());
		
		test.hardReset();
		test.setLevel(ResetActions.HARD);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		assertEquals(JobState.READY, job.lastStateEvent().getState());
		
	}
	
   @Test
	public void testResetForceExample() throws IOException {
		
		File file = new File(getClass().getResource(
				"ResetForceExample.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		ConsoleCapture console = new ConsoleCapture();
		
		try (ConsoleCapture.Close closeable = console.captureConsole()) {
		
			oddjob.run();		
		}
		
        assertEquals(ParentState.COMPLETE, 
                oddjob.lastStateEvent().getState());
        
        assertEquals(0, console.getLines().length);
        
		oddjob.destroy();
	}
}
