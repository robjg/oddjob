package org.oddjob.jobs.job;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.OjTestCase;
import org.oddjob.Stoppable;
import org.oddjob.state.JobState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.Matchers.is;

public class StopJobTest extends OjTestCase {

	private static class MyStoppable implements Stoppable {
		
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
		
		MatcherAssert.assertThat(stoppable.stopped, is(true));
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

	@Test
	public void testLoopbackStopAsync() {

		ExecutorService executorService = Executors.newSingleThreadExecutor();

		StopJob test = new StopJob();
		test.setExecutorService(executorService);
		test.setJob(test);

		test.run();

		MatcherAssert.assertThat(test.lastStateEvent().getState(), is(JobState.COMPLETE));
	}

}
