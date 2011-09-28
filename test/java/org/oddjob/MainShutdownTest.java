package org.oddjob;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.io.BufferType;
import org.oddjob.jobs.ExecJob;
import org.oddjob.jobs.WaitJob;
import org.oddjob.state.JobState;
import org.oddjob.state.State;
import org.oddjob.state.StateConditions;

public class MainShutdownTest extends TestCase {

	private static final Logger logger = Logger.getLogger(MainTest.class);
	
	@Override
	protected void setUp() throws Exception {
		logger.debug("------------------ " + getName() + " -----------------");
	}
	
	public static class OurSimpleJob extends SimpleJob
	implements Stoppable {
		
		boolean stopped;
		Thread t;
		
		@Override
		protected int execute() throws Throwable {
			t = Thread.currentThread();
			synchronized (this) {
				try {
					while (true) {
						wait();
					}
				} catch (InterruptedException e) {
					logger.debug("OurSimpleJob interrupted.");
				}
			}
			return 0;
		}
		public void onStop() {
			stopped = true;
			t.interrupt();
		}
	}
	
	public void testShutdownHook() throws Exception {
		
		String xml = "<oddjob>" +
				" <job>" +
				"  <bean id='r' class='" + OurSimpleJob.class.getName() + "'/>" +
				" </job>" +
				"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		Thread t = new Thread(oj);
		t.start();
		
		OurSimpleJob r = null;
		while (r == null) {
			Thread.sleep(1000);
			r = (OurSimpleJob) new OddjobLookup(oj).lookup("r");
		}
		
		OddjobRunner.ShutdownHook hook = new OddjobRunner(oj). new ShutdownHook();
		hook.run();
		hook.killer.interrupt();
		
		assertTrue(r.stopped);
	}
	
	public static class NaughtyJob implements Runnable {
		
		@Override
		public void run() {
			
			WaitJob wait = new WaitJob() {
				@Override
				public int execute() throws Exception {
					System.out.println("Naughty Thread Started.");
					return super.execute();
				}
			};
			new Thread(wait).start();
		}
	}
	
	/** This doesn't test what we'd hoped because process destroy
	 *  appears to kill the jvm without invoking the shutdown hook.
	 *  
	 * @throws FailedToStopException
	 * @throws InterruptedException
	 */
	public void testKillerThread() throws FailedToStopException, InterruptedException {
		
		OurDirs dirs = new OurDirs();
		
		File testClasses = dirs.relative("classes");
		if (!testClasses.exists()) {
			testClasses = dirs.relative("build/test");
		}
		if (!testClasses.exists()) {
			fail ("No test classes!");
		}
		
		ConsoleCapture console = new ConsoleCapture();
		
		ExecJob exec = new ExecJob();
		
		exec.setArgs(new String[] {
				"java", 
				"-D" + OddjobRunner.KILLER_TIMEOUT_PROPERTY + "=1",
				"-jar",
				dirs.relative("run-oddjob.jar").getPath(),
				"-cp",
				testClasses.getPath(),
				"-f",
				dirs.relative(
						"test/conf/test-killer.xml").getPath()
		});
		
		console.capture(exec.consoleLog());
		
		new Thread(exec).start();
		
		console.dump(logger);
		
		WaitJob wait = new WaitJob();
		wait.setFor(exec);
		wait.setState(StateConditions.EXECUTING);
		
		wait.run();
		
		while (true) {
			Thread.sleep(500);
			
			BufferType buffer = new BufferType();
			buffer.setLines(console.getLines());
			buffer.configured();
			
			State jobState = exec.lastStateEvent().getState();
			if (buffer.getText().contains("Naughty Thread Started.")) {
				break;
			}
			else {
				if (JobState.EXECUTING != jobState) {
					console.dump(logger);
					fail("Something wrong.");
				}
			}
			
			logger.info("Waiting for console.");
		}

		exec.stop();
				
		console.dump(logger);
		
		assertEquals(1, exec.getExitValue());
		
		assertEquals(JobState.INCOMPLETE, 
				exec.lastStateEvent().getState());
		
		console.close();
	}
	
}
