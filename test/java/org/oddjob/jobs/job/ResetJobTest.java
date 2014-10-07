package org.oddjob.jobs.job;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;

public class ResetJobTest extends TestCase {

	private static final Logger logger = Logger.getLogger(
			ResetJobTest .class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.info("-------------------------  " + getName() + 
				"  ------------------------");
	}
	
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
		test.setLevel("HARD");
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		assertEquals(JobState.READY, job.lastStateEvent().getState());
		
	}
	
	public void testResetForceExample() {
		
		File file = new File(getClass().getResource(
				"ResetForceExample.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		oddjob.run();
		
		console.close();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		assertEquals(0, console.getLines().length);
		
		oddjob.destroy();
	}
}
