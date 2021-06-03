package org.oddjob.jobs.job;

import org.junit.Assert;
import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.state.JobState;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StopJobTest {

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

        assertThat(stoppable.stopped, is(true));
        assertThat(test.lastStateEvent().getState(), is(JobState.COMPLETE));
    }

    @Test
    public void testLoopbackStop() {

        StopJob test = new StopJob();
        test.setJob(test);

        test.run();

        assertThat(test.lastStateEvent().getState(), is(JobState.COMPLETE));
    }

    @Test
    public void testLoopbackStopAsync() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        StopJob test = new StopJob();
        test.setExecutorService(executorService);
        test.setJob(test);

        test.run();

        assertThat(test.lastStateEvent().getState(), is(JobState.COMPLETE));

        executorService.shutdown();
    }

	private static class MyUnStoppable implements Stoppable {

		final CountDownLatch stopped = new CountDownLatch(1);

		@Override
		public void stop() throws FailedToStopException {
			this.stopped.countDown();
			try {
				Thread.sleep(Long.MAX_VALUE);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Test
	public void testUnstoppable() {

		ExecutorService executorService = Executors.newSingleThreadExecutor();

		MyUnStoppable job = new MyUnStoppable();

		StopJob test = new StopJob();
		test.setExecutorService(executorService);
		test.setJob(job);

		Thread t = new Thread(() -> {
			try {
				job.stopped.await();
				test.stop();
			} catch (InterruptedException | FailedToStopException e) {
				Assert.fail(e.getMessage());
			}
		});
		t.start();

		test.run();

		assertThat(test.lastStateEvent().getState(), is(JobState.INCOMPLETE));

		executorService.shutdown();
	}

}
