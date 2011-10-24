package org.oddjob.jobs.job;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;

public class StopJobTest extends TestCase {

	private class MyStoppable implements Stoppable {
		
		boolean stopped;
		
		@Override
		public void stop() throws FailedToStopException {
			this.stopped = true;
		}		
	}
	
	public void testSimpleStop() {
		
		MyStoppable stoppable = new MyStoppable();
		
		StopJob test = new StopJob();
		test.setJob(stoppable);
		
		test.run();
		
		assertEquals(true, stoppable.stopped);
	}
}
