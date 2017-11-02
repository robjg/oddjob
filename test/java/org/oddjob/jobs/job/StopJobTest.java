package org.oddjob.jobs.job;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.state.JobState;

public class StopJobTest extends OjTestCase {

	private class MyStoppable implements Stoppable {
		
		boolean stopped;
		
		@Override
		public void stop() throws FailedToStopException {
			this.stopped = true;
		}		
	}
	
   @Test
	public void testSimpleStop() {
		
		MyStoppable stoppable = new MyStoppable();
		
		StopJob test = new StopJob();
		test.setJob(stoppable);
		
		test.run();
		
		assertEquals(true, stoppable.stopped);
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
	}
	
   @Test
	public void testLoopbackStop() {
		
		StopJob test = new StopJob();
		test.setJob(test);
		
		test.run();
		
		assertEquals(JobState.EXCEPTION, test.lastStateEvent().getState());
		assertEquals(FailedToStopException.class, 
				test.lastStateEvent().getException().getClass());
	}
}
