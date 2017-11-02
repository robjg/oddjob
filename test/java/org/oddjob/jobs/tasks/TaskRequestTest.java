package org.oddjob.jobs.tasks;
import org.junit.Before;

import org.junit.Test;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.input.InputRequest;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;

public class TaskRequestTest extends OjTestCase {

	private static final Logger logger = Logger.getLogger(TaskRequestTest.class);
	
    @Before
    public void setUp() throws Exception {

		
		logger.info("--------------------  " + getName() + "  ------------------");
	}
	
   @Test
	public void testSimpleExample() {
		
		File file = new File(getClass().getResource("TaskRequestSimple.xml"
				).getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		ConsoleCapture capture = new ConsoleCapture();
		try (ConsoleCapture.Close close = capture.captureConsole()) {
			
			oddjob.run();
		}
		
		assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());
		
		String[] lines = capture.getLines();
		
		assertEquals("Hello Rod.", lines[0].trim());
		assertEquals("Hello Jane.", lines[1].trim());
		assertEquals("Hello Freddy.", lines[2].trim());

		oddjob.destroy();
	}
	
	private class OurTaskExecutor implements TaskExecutor {
		
		private FlagState job = new FlagState(JobState.EXCEPTION);
		
		private CountDownLatch countDown = new CountDownLatch(1);
		
		@Override
		public InputRequest[] getParameterInfo() {
			throw new RuntimeException("Unexpected");
		}
		
		@Override
		public TaskView execute(Task task) throws TaskException {
			try {
				return new JobTaskView(job) {
					@Override
					protected Object onDone() {
						throw new RuntimeException("Unexpected");
					}
				};
			}
			finally {
				countDown.countDown();
			}
		}
	}

   @Test
	public void testAsynchronousTaskException() throws InterruptedException {
		
		OurTaskExecutor taskExecutor = new OurTaskExecutor();
		
		final TaskRequest test = new TaskRequest(); 
		test.setTaskExecutor(taskExecutor);
		
		Thread t = new Thread(test);
		t.start();
		
		taskExecutor.countDown.await();
		
		taskExecutor.job.run();
		
		t.join();
		
		assertEquals(JobState.EXCEPTION, test.lastStateEvent().getState());
	}
}
