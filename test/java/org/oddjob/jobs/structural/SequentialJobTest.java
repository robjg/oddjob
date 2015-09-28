/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs.structural;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobComponentResolver;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.persist.MapPersister;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.MirrorState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.StateSteps;

/**
 * 
 */
public class SequentialJobTest extends TestCase {

	private static final Logger logger = Logger.getLogger(SequentialJobTest.class);
	
	public static class OurJob extends SimpleJob {

		@Override
		protected int execute() throws Throwable {
			return 0;
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("---------------------------------  " + getName() + 
				"  ----------------------------------");
	}
	
	
	// an empty sequence must be ready. This is to agree with oddjob
	// which must also be ready when reset and empty.
	// this is really a bug in StatefulChildHelper. An empty sequence should
	// be ready until run and then be complete. I think.
	public void testEmpty() {
		SequentialJob test = new SequentialJob();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		
		test.run();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());	
	}
	
	// a sequence of just objects will always be complete when it runs
	public void testObject() {
		SequentialJob test = new SequentialJob();
		
		test.setJobs(0, (new Object()));
		test.setJobs(1, (new Object()));
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		test.run();
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());	
		
	}
	
	public void testTriggers() {
		OurJob j1 = new OurJob();
		MirrorState t1 = new MirrorState();
		t1.setJob((Stateful) j1);
		t1.run();
		
		OurJob j2 = new OurJob();
		MirrorState t2 = new MirrorState();
		t2.setJob((Stateful) j2);
		t2.run();
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, t1);
		test.setJobs(1, t2);
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		
		test.run();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());	
		
		((Runnable) j1).run();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());	
		
		((Runnable) j2).run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());	
		
		((Resetable) j2).hardReset();

		assertEquals(ParentState.READY, test.lastStateEvent().getState());	
	}
	
	public void testTwoRunnableJobs() {
		OurJob j1 = new OurJob();
		
		OurJob j2 = new OurJob();
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, j1);
		test.setJobs(1, j2);
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());	
		
		((Resetable) j2).hardReset();

		assertEquals(ParentState.READY, test.lastStateEvent().getState());			
	}
	
	/**
	 * Test a mixture of Objects and jobs.
	 */
	public void testMixtureOfJobMirrorAndObject() {
		OurJob j1 = new OurJob();
		
		Object j2 = new OurJob();
		
		MirrorState t2 = new MirrorState();
		t2.setJob((Stateful) j2);
		t2.run();
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, j1);
		test.setJobs(1, t2);
		test.setJobs(2, new Object());

		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		test.run();
		assertEquals(ParentState.READY, test.lastStateEvent().getState());	
		
		((Runnable) j2).run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());	
		
		test.hardReset();

		assertEquals(ParentState.READY, test.lastStateEvent().getState());			
	}
	
	public void testOneJobCompleteOneJobNotComplete() {
		FlagState j1 = new FlagState();
		j1.setState(JobState.COMPLETE);
		
		FlagState j2 = new FlagState();
		j2.setState(JobState.INCOMPLETE);
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, j1);
		test.setJobs(1, j2);

		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		test.run();
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());	
		
		test.hardReset();

		assertEquals(ParentState.READY, test.lastStateEvent().getState());			
		
	}
	
	public void testDependentProgression() {
		FlagState j1 = new FlagState();
		j1.setState(JobState.INCOMPLETE);
		
		FlagState j2 = new FlagState();
		j2.setState(JobState.COMPLETE);
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, j1);
		test.setJobs(1, j2);

		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		test.run();
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());	
		assertEquals(JobState.INCOMPLETE, j1.lastStateEvent().getState());
		
		// j2 should not be attempted in dependent sequential job
		assertEquals(JobState.READY, j2.lastStateEvent().getState());	
		
		test.hardReset();

		assertEquals(ParentState.READY, test.lastStateEvent().getState());			
		
	}
	
	public void testIndependentProgression() {
		FlagState j1 = new FlagState();
		j1.setState(JobState.INCOMPLETE);
		
		FlagState j2 = new FlagState();
		j2.setState(JobState.COMPLETE);
		
		SequentialJob test = new SequentialJob();
		
		test.setIndependent(true);
		
		test.setJobs(0, j1);
		test.setJobs(1, j2);
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		test.run();
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());	
		assertEquals(JobState.INCOMPLETE, j1.lastStateEvent().getState());
		
		// j2 should be attempted and complete in independent sequential job
		assertEquals(JobState.COMPLETE, j2.lastStateEvent().getState());	
		
		test.hardReset();

		assertEquals(ParentState.READY, test.lastStateEvent().getState());			
		
	}
	
	public void testException() {
		FlagState j1 = new FlagState();
		j1.setState(JobState.COMPLETE);
		
		FlagState j2 = new FlagState();
		j2.setState(JobState.EXCEPTION);
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, j1);
		test.setJobs(1, j2);

		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		test.run();
		
		assertEquals(ParentState.EXCEPTION, test.lastStateEvent().getState());	
		
		test.hardReset();

		assertEquals(ParentState.READY, test.lastStateEvent().getState());			
		
	}
	
	public void testWhenSequentialJobDestroyedStateIsDestroyed() {
		
		FlagState j1 = new FlagState();
		j1.setState(JobState.COMPLETE);
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, j1);

		StateSteps sequentialState = new StateSteps(test);
		sequentialState.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.COMPLETE);
		
		test.run();
		
		sequentialState.checkNow();
				
		sequentialState.startCheck(ParentState.COMPLETE, ParentState.DESTROYED);
		
		test.destroy();

		sequentialState.checkNow();
	}
	
	public void testStatesWhenOddjobDestroyed() throws ArooaPropertyException, ArooaConversionException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential id='sequential'>" +
			"   <jobs>" +
			"    <state:flag xmlns:state='http://rgordon.co.uk/oddjob/state'/>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		Stateful sequential = new OddjobLookup(oddjob).lookup(
				"sequential", Stateful.class);
		
		StateSteps state = new StateSteps(sequential);
		state.startCheck(ParentState.COMPLETE, ParentState.DESTROYED);
		
		oddjob.destroy();
		
		state.checkNow();
	}
	
	public static class MyService {
		
		public void start() {}
		public void stop() {}
	}
	
	public void testAServiceAndAJob() throws FailedToStopException {
		
		Object service = new OddjobComponentResolver().resolve(
				new MyService(), new StandardArooaSession());
		
		FlagState job = new FlagState();
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, service);
		test.setJobs(0, job);

		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.STARTED);
		
		test.run();
		
		states.checkNow();
		
		assertEquals(JobState.COMPLETE, job.lastStateEvent().getState());
		
		states.startCheck(ParentState.STARTED, ParentState.COMPLETE);

		test.stop();
		
		states.checkNow();
	}
	
	public void testTwoServices() throws FailedToStopException {
		
		Object service1 = new OddjobComponentResolver().resolve(
				new MyService(), new StandardArooaSession());
		
		Object service2 = new OddjobComponentResolver().resolve(
				new MyService(), new StandardArooaSession());
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, service1);
		test.setJobs(0, service2);

		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.STARTED);
		
		test.run();
		
		states.checkNow();
		
		assertEquals(ServiceState.STARTED, 
				((Stateful) service1).lastStateEvent().getState());
		assertEquals(ServiceState.STARTED, 
				((Stateful) service2).lastStateEvent().getState());
				
		// Stop each service. Sequential should go to complete
		// only when both are stopped.
		
		states.startCheck(ParentState.STARTED, ParentState.COMPLETE);

		((Stoppable) service1).stop();
		
		assertEquals(ParentState.STARTED, test.lastStateEvent().getState());
		
		((Stoppable) service2).stop();
		
		states.checkNow();
		
		// Check that starting a service sequential reflects started.
		
		states.startCheck(ParentState.COMPLETE, ParentState.READY,
				ParentState.ACTIVE, ParentState.STARTED);
		
		((Resetable) service1).hardReset();
		((Runnable) service1).run();
		
		states.checkNow();
		
		// Check stopping sequential.
		
		states.startCheck(ParentState.STARTED, ParentState.COMPLETE);

		test.stop();
		
		states.checkNow();
			
	}
	
	public void testNestedSequentials() throws FailedToStopException {
		
		Object service = new OddjobComponentResolver().resolve(
				new MyService(), new StandardArooaSession());
		
		FlagState job1 = new FlagState();
		
		SequentialJob sequential1 = new SequentialJob();
		sequential1.setJobs(0, service);
		sequential1.setJobs(0, job1);

		SequentialJob sequential2 = new SequentialJob();
		FlagState job2 = new FlagState();
		sequential2.setJobs(0, job2);
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, sequential1);
		test.setJobs(1, sequential2);
		
		StateSteps states = new StateSteps(sequential1);
		states.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.STARTED);
		
		test.run();
		
		states.checkNow();
		
		assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
		
		states.startCheck(ParentState.STARTED, ParentState.COMPLETE);

		test.stop();
		
		states.checkNow();		
	}
	
	public void testExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/structural/SimpleSequentialExample.xml", 
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.captureConsole();
		
		StateSteps steps = new StateSteps(oddjob);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING,
				ParentState.COMPLETE);		
		
		oddjob.run();		
		
		steps.checkNow();
		
		console.close();
		
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals(2, lines.length);
				
		assertEquals("This runs first.", lines[0].trim());
		assertEquals("This runs after.", lines[1].trim());
				
		oddjob.destroy();	
	}

	public void testPersistence() throws ArooaPropertyException, ArooaConversionException {
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <sequential id='seq'>" +
				"   <jobs>" +
				"    <state:flag xmlns:state='http://rgordon.co.uk/oddjob/state'/>" +
				"   </jobs>" +
				"  </sequential>" +
				" </job>" +
				"</oddjob>";
				 
		MapPersister persister = new MapPersister();
		
		{
			Oddjob oddjob1 = new Oddjob();
			oddjob1.setConfiguration(new XMLConfiguration("XML", xml));
			oddjob1.setPersister(persister);
			oddjob1.run();
			
			assertEquals(ParentState.COMPLETE, oddjob1.lastStateEvent().getState());
					
			oddjob1.destroy();
		}
		
		{
			Oddjob oddjob2 = new Oddjob();
			oddjob2.setConfiguration(new XMLConfiguration("XML", xml));
			oddjob2.setPersister(persister);
			
			oddjob2.load();
			
			assertEquals(ParentState.READY, oddjob2.lastStateEvent().getState());
			
			OddjobLookup lookup = new OddjobLookup(oddjob2);
			
			Stateful seq = lookup.lookup("seq", Stateful.class);
			
			assertEquals(ParentState.COMPLETE, seq.lastStateEvent().getState());
		}		
	}
	
	public void testPersistenceWhenTransient() throws ArooaPropertyException, ArooaConversionException {
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <sequential id='seq' transient='true'>" +
				"   <jobs>" +
				"    <state:flag xmlns:state='http://rgordon.co.uk/oddjob/state'/>" +
				"   </jobs>" +
				"  </sequential>" +
				" </job>" +
				"</oddjob>";
				 
		MapPersister persister = new MapPersister();
		
		{
			Oddjob oddjob1 = new Oddjob();
			oddjob1.setConfiguration(new XMLConfiguration("XML", xml));
			oddjob1.setPersister(persister);
			oddjob1.run();
			
			assertEquals(ParentState.COMPLETE, oddjob1.lastStateEvent().getState());
					
			oddjob1.destroy();
		}
		
		{
			Oddjob oddjob2 = new Oddjob();
			oddjob2.setConfiguration(new XMLConfiguration("XML", xml));
			oddjob2.setPersister(persister);
			
			oddjob2.load();
			
			assertEquals(ParentState.READY, oddjob2.lastStateEvent().getState());
			
			OddjobLookup lookup = new OddjobLookup(oddjob2);
			
			Stateful seq = lookup.lookup("seq", Stateful.class);
			
			assertEquals(ParentState.READY, seq.lastStateEvent().getState());
		}		
	}
}
