/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.state;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.StateSteps;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.jobs.WaitJob;

public class IfJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(IfJobTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("--------------  " + getName() + "  ----------------");
	}
	
	public void testIfOnlyJobComplete() {
		FlagState child = new FlagState();
		child.setState(JobState.COMPLETE);
		
		IfJob test = new IfJob();
		test.setJobs(0, child);
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		test.hardReset();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
	}
	
	public void testIfOnlyJobNotComplete() {
		FlagState child = new FlagState();
		child.setState(JobState.INCOMPLETE);
		
		IfJob test = new IfJob();
		test.setJobs(0, child);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		test.hardReset();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
	}
	
	public void testIfOnlyJobException() {
		FlagState child = new FlagState();
		child.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setJobs(0, child);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		test.hardReset();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
	}
	
	class OurJob extends SimpleJob {
		private JobState desired;
		private int count;	
		
		protected int execute() throws Exception {
			++count;
			if (desired.equals(JobState.COMPLETE)) {
				return 0;
			}
			if (desired.equals(JobState.INCOMPLETE)) {
				return 1;
			}
			throw new Exception("An exception.");
		}

		public void setDesired(JobState desired) {
			this.desired = desired;
		}
	}
	
	public void testThen1() throws IOException, ClassNotFoundException {
		FlagState depends = new FlagState();
		depends.setState(JobState.COMPLETE);
		depends.run();
		
		OurJob then = new OurJob();
		then.setDesired(JobState.COMPLETE);
		
		IfJob test = new IfJob();
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, then.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		IfJob copy = (IfJob) Helper.copy(test);
		copy.setJobs(0, depends);
		copy.setJobs(1, then);
		
		assertEquals(JobState.COMPLETE, copy.lastJobStateEvent().getJobState());
		
		copy.hardReset();
		copy.run();
		
		assertEquals(JobState.COMPLETE, copy.lastJobStateEvent().getJobState());
		
		assertEquals(2, then.count);
		
		then.hardReset();
		
		assertEquals(JobState.READY, copy.lastJobStateEvent().getJobState());

	}
	
	public void testThen2() {
		FlagState depends = new FlagState();
		depends.setState(JobState.COMPLETE);
		depends.run();
		
		FlagState then = new FlagState();
		then.setState(JobState.INCOMPLETE);
		
		IfJob j = new IfJob();
		j.setJobs(0, depends);
		j.setJobs(1, then);
		
		j.run();
		
		assertEquals(JobState.INCOMPLETE, j.lastJobStateEvent().getJobState());
	}

	public void testThen3() {
		FlagState depends = new FlagState();
		depends.setState(JobState.COMPLETE);
		
		FlagState then = new FlagState();
		then.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(JobState.EXCEPTION, test.lastJobStateEvent().getJobState());
		
		then.softReset();
	
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, depends.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, then.lastJobStateEvent().getJobState());

		depends.hardReset();
		
		assertEquals(JobState.READY, depends.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		test.hardReset();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
	}

	public void testNotThen() {
		FlagState depends = new FlagState();
		depends.setState(JobState.COMPLETE);
		depends.run();
		
		FlagState then = new FlagState();
		then.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setNot(true);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, depends.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, then.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());		
	}
	
	public void testNotThen2() {
		FlagState depends = new FlagState();
		depends.setState(JobState.COMPLETE);
		depends.run();
		
		FlagState then = new FlagState();
		then.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setState(JobState.INCOMPLETE);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, depends.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, then.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());		
	}
	
	public void testElse1() {
		FlagState depends = new FlagState();
		depends.setState(JobState.INCOMPLETE);
		depends.setName("Depends On");
//		depends.run();
		
		FlagState then = new FlagState(JobState.EXCEPTION);
		then.setName("Unexpected Then");
		
		OurJob elze = new OurJob();
		elze.setDesired(JobState.COMPLETE);
		
		IfJob test = new IfJob();
		test.setJobs(0, depends);
		test.setJobs(1, then);
		test.setJobs(2, elze);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		test.hardReset();
		
		elze.setDesired(JobState.INCOMPLETE);
		
		test.run();
		
		assertEquals(JobState.INCOMPLETE, test.lastJobStateEvent().getJobState());
		
		assertEquals(2, elze.count);
		
		test.softReset();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, elze.lastJobStateEvent().getJobState());
	}
	
	public void testElse2() {
		FlagState depends = new FlagState();
		depends.setState(JobState.INCOMPLETE);
		depends.run();
		
		FlagState then = new FlagState(JobState.EXCEPTION);
		then.setName("Unexpected Then");
		
		FlagState elze = new FlagState();
		elze.setState(JobState.COMPLETE);
		
		IfJob test = new IfJob();
		test.setJobs(0, depends);
		test.setJobs(1, then);
		test.setJobs(2, elze);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
	}

	public void testElse3() {
		FlagState depends = new FlagState();
		depends.setState(JobState.EXCEPTION);
		depends.run();

		FlagState then = new FlagState(JobState.COMPLETE);
		then.setName("Unexpected Then");
		
		FlagState elze = new FlagState();
		elze.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setState(JobState.INCOMPLETE);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		test.setJobs(2, elze);
		
		test.run();
		
		assertEquals(JobState.EXCEPTION, test.lastJobStateEvent().getJobState());
	}
	
	// only an else job but the child completes.
	public void testNoElse() {
		FlagState depends = new FlagState();
		depends.setState(JobState.COMPLETE);
		depends.run();
		
		FlagState then = new FlagState();
		then.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setState(JobState.INCOMPLETE);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
	}
	
	public void testException1() {
		FlagState depends = new FlagState();
		depends.setState(JobState.EXCEPTION);
		depends.run();
		
		OurJob then = new OurJob();
		then.setDesired(JobState.COMPLETE);
		
		IfJob test = new IfJob();
		test.setState(JobState.EXCEPTION);		
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		test.hardReset();
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		assertEquals(2, then.count);
		
	}
	
	public void testException2() {
		FlagState depends = new FlagState();
		depends.setState(JobState.EXCEPTION);
		depends.run();
		
		FlagState then = new FlagState();
		then.setState(JobState.INCOMPLETE);
		
		IfJob test = new IfJob();
		test.setState(JobState.EXCEPTION);
		
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(JobState.INCOMPLETE, test.lastJobStateEvent().getJobState());
	}
	
	public void testException3() {
		FlagState depends = new FlagState();
		depends.setState(JobState.EXCEPTION);
		depends.run();
				
 		FlagState then = new FlagState();
		then.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setState(JobState.EXCEPTION);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(JobState.EXCEPTION, test.lastJobStateEvent().getJobState());
	}
	
	public void testNotException() {
		FlagState depends = new FlagState();
		depends.setState(JobState.INCOMPLETE);
		depends.run();
				
 		FlagState then = new FlagState();
		then.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setState(JobState.EXCEPTION);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
	}
	
	public void testNotExceptionWithElse() {
		FlagState depends = new FlagState();
		depends.setState(JobState.INCOMPLETE);
		depends.run();
				
 		FlagState then = new FlagState();
		then.setState(JobState.EXCEPTION);
		
 		FlagState elze = new FlagState();
		elze.setState(JobState.COMPLETE);
		
		IfJob test = new IfJob();
		test.setState(JobState.EXCEPTION);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		test.setJobs(2, elze);
		
		test.run();
		
		assertEquals(JobState.READY, then.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, elze.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
	}
	
	public void testInOddjob() {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("Resource",
				IfJobTest.class.getResourceAsStream("if.xml")));

		oj.run();
		
		assertEquals(JobState.COMPLETE, oj.lastJobStateEvent().getJobState());
		
		oj.destroy();
	}
	
	
	public void testNotComplete() throws IOException, ClassNotFoundException {
		FlagState depends = new FlagState();
		depends.setState(JobState.INCOMPLETE);
		
		OurJob then = new OurJob();
		then.setDesired(JobState.COMPLETE);

		IfJob test = new IfJob();
		test.setJobs(0, depends);
		test.setJobs(1, then);
		test.setNot(true);
		test.run();
		
		assertEquals(JobState.COMPLETE, then.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());		
	}

	public void testNotNotComplete() throws IOException, ClassNotFoundException {
		FlagState depends = new FlagState();
		depends.setState(JobState.COMPLETE);
		
		OurJob then = new OurJob();
		then.setDesired(JobState.EXCEPTION);

		StateSteps thenState = new StateSteps(then);
		thenState.startCheck(JobState.READY);
		
		IfJob test = new IfJob();
		
		StateSteps testState = new StateSteps(test);
		testState.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);
		
		test.setNot(true);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();

		thenState.checkNow();
		testState.checkNow();
	}
	
	public void testNotInComplete() throws IOException, ClassNotFoundException {
		FlagState depends = new FlagState();
		depends.setState(JobState.INCOMPLETE);
		depends.run();
		
		OurJob then = new OurJob();
		then.setDesired(JobState.COMPLETE);

		IfJob test = new IfJob();
		test.setJobs(0, depends);
		test.setJobs(1, then);
		test.setNot(true);
		test.setState(JobState.INCOMPLETE);
		test.run();
		
		assertEquals(JobState.READY, then.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());		
	}
	
	public void testReset() {

		FlagState depends = new FlagState();
		depends.setState(JobState.COMPLETE);
		depends.run();
		
		OurJob then = new OurJob();
		then.setDesired(JobState.COMPLETE);

		IfJob test = new IfJob();
		test.setJobs(0, depends);
		test.setJobs(1, then);

		test.hardReset();
		
		assertEquals(JobState.READY, then.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());		
		
	}
	
	public void testStop() throws IOException, ClassNotFoundException, FailedToStopException, InterruptedException {
		WaitJob depends = new WaitJob();
		
		OurJob then = new OurJob();
		then.setDesired(JobState.COMPLETE);
		
		IfJob test = new IfJob();
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		StateSteps dependsState = new StateSteps(depends);
		dependsState.startCheck(JobState.READY, JobState.EXECUTING);
		
		Thread t = new Thread(test);
		t.start();
		
		dependsState.checkWait();
		
		test.stop();
		
		t.join();
		
		assertEquals(JobState.READY, then.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
	}
}
