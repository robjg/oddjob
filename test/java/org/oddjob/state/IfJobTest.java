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
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		test.hardReset();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
	}
	
	public void testIfOnlyJobNotComplete() {
		FlagState child = new FlagState();
		child.setState(JobState.INCOMPLETE);
		
		IfJob test = new IfJob();
		test.setJobs(0, child);
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		test.hardReset();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
	}
	
	public void testIfOnlyJobException() {
		FlagState child = new FlagState();
		child.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setJobs(0, child);
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		test.hardReset();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
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
		
		assertEquals(JobState.COMPLETE, then.lastStateEvent().getState());
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		IfJob copy = (IfJob) Helper.copy(test);
		copy.setJobs(0, depends);
		copy.setJobs(1, then);
		
		assertEquals(ParentState.COMPLETE, copy.lastStateEvent().getState());
		
		copy.hardReset();
		copy.run();
		
		assertEquals(ParentState.COMPLETE, copy.lastStateEvent().getState());
		
		assertEquals(2, then.count);
		
		then.hardReset();
		
		assertEquals(ParentState.READY, copy.lastStateEvent().getState());

	}
	
	public void testThen2() {
		FlagState depends = new FlagState();
		depends.setState(JobState.COMPLETE);
		depends.run();
		
		FlagState then = new FlagState();
		then.setState(JobState.INCOMPLETE);
		
		IfJob test = new IfJob();
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
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
		
		assertEquals(ParentState.EXCEPTION, test.lastStateEvent().getState());
		
		then.softReset();
	
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, depends.lastStateEvent().getState());
		assertEquals(JobState.READY, then.lastStateEvent().getState());

		depends.hardReset();
		
		assertEquals(JobState.READY, depends.lastStateEvent().getState());
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		test.hardReset();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
	}

	public void testNotThen() {
		FlagState depends = new FlagState();
		depends.setState(JobState.COMPLETE);
		depends.run();
		
		FlagState then = new FlagState();
		then.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setState(new IsNot(StateConditions.COMPLETE));
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, depends.lastStateEvent().getState());
		assertEquals(JobState.READY, then.lastStateEvent().getState());
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());		
	}
	
	public void testNotThen2() {
		FlagState depends = new FlagState();
		depends.setState(JobState.COMPLETE);
		depends.run();
		
		FlagState then = new FlagState();
		then.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setState(StateConditions.INCOMPLETE);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, depends.lastStateEvent().getState());
		assertEquals(JobState.READY, then.lastStateEvent().getState());
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());		
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
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		test.hardReset();
		
		elze.setDesired(JobState.INCOMPLETE);
		
		test.run();
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
		
		assertEquals(2, elze.count);
		
		test.softReset();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		assertEquals(JobState.READY, elze.lastStateEvent().getState());
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
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
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
		test.setState(StateConditions.INCOMPLETE);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		test.setJobs(2, elze);
		
		test.run();
		
		assertEquals(ParentState.EXCEPTION, test.lastStateEvent().getState());
	}
	
	// only an else job but the child completes.
	public void testNoElse() {
		FlagState depends = new FlagState();
		depends.setState(JobState.COMPLETE);
		depends.run();
		
		FlagState then = new FlagState();
		then.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setState(StateConditions.INCOMPLETE);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
	}
	
	public void testException1() {
		FlagState depends = new FlagState();
		depends.setState(JobState.EXCEPTION);
		depends.run();
		
		OurJob then = new OurJob();
		then.setDesired(JobState.COMPLETE);
		
		IfJob test = new IfJob();
		test.setState(StateConditions.EXCEPTION);		
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		test.hardReset();
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		assertEquals(2, then.count);
	}
	
	public void testException2() {
		FlagState depends = new FlagState();
		depends.setState(JobState.EXCEPTION);
		depends.run();
		
		FlagState then = new FlagState();
		then.setState(JobState.INCOMPLETE);
		
		IfJob test = new IfJob();
		test.setState(StateConditions.EXCEPTION);
		
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
	}
	
	public void testException3() {
		FlagState depends = new FlagState();
		depends.setState(JobState.EXCEPTION);
		depends.run();
				
 		FlagState then = new FlagState();
		then.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setState(StateConditions.EXCEPTION);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(ParentState.EXCEPTION, test.lastStateEvent().getState());
	}
	
	public void testNotException() {
		FlagState depends = new FlagState();
		depends.setState(JobState.INCOMPLETE);
		depends.run();
				
 		FlagState then = new FlagState();
		then.setState(JobState.EXCEPTION);
		
		IfJob test = new IfJob();
		test.setState(StateConditions.EXCEPTION);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
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
		test.setState(StateConditions.EXCEPTION);
		test.setJobs(0, depends);
		test.setJobs(1, then);
		test.setJobs(2, elze);
		
		test.run();
		
		assertEquals(JobState.READY, then.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, elze.lastStateEvent().getState());
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
	}
	
	public void testInOddjob() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("Resource",
				IfJobTest.class.getResourceAsStream("if.xml")));

		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
	
	
	public void testNotComplete() throws IOException, ClassNotFoundException {
		FlagState depends = new FlagState();
		depends.setState(JobState.INCOMPLETE);
		
		OurJob then = new OurJob();
		then.setDesired(JobState.COMPLETE);

		IfJob test = new IfJob();
		test.setJobs(0, depends);
		test.setJobs(1, then);
		test.setState(new IsNot(StateConditions.COMPLETE));
		test.run();
		
		assertEquals(JobState.COMPLETE, then.lastStateEvent().getState());
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());		
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
		testState.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.COMPLETE);
		
		test.setState(new IsNot(StateConditions.COMPLETE));
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
		test.setState(new IsNot(StateConditions.INCOMPLETE));
		test.run();
		
		assertEquals(JobState.READY, then.lastStateEvent().getState());
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());		
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
		
		assertEquals(JobState.READY, then.lastStateEvent().getState());
		assertEquals(ParentState.READY, test.lastStateEvent().getState());		
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
		
		assertEquals(JobState.READY, then.lastStateEvent().getState());
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
	}
}
