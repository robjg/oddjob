/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs.job;

import java.beans.PropertyVetoException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
import org.oddjob.IconSteps;
import org.oddjob.Iconic;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.images.IconHelper;
import org.oddjob.jobs.WaitJob;
import org.oddjob.jobs.structural.SequentialJob;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateHandler;
import org.oddjob.state.StateListener;
import org.oddjob.util.OddjobLockedException;

/**
 * 
 */
public class RunJobTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(RunJobTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("------------------------  " + getName() + 
				"  ---------------------------");
	}
	
	public static class OurRunnable implements Runnable {
		int ran;
		
		public void run() {
			++ran;
		}
		public int getRan() {
			return ran;
		}
	}
	
	public void testCode() {
		
		StandardArooaSession session = new StandardArooaSession();
		
		OurRunnable r = new OurRunnable();
		
		RunJob test = new RunJob();
		test.setArooaSession(session);
		test.setJob(r);
		test.run();
		
		assertEquals(ParentState.COMPLETE, 
				test.lastStateEvent().getState());
		assertEquals(1, r.ran);
		
		test.hardReset();
		
		assertEquals(ParentState.READY, 
				test.lastStateEvent().getState());
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, 
				test.lastStateEvent().getState());
		
		assertEquals(2, r.ran);
		
		Object[] children = Helper.getChildren(test);
		assertEquals(1, children.length);
		Object proxy = children[0];

		assertEquals(JobState.COMPLETE, 
				((Stateful) proxy).lastStateEvent().getState());
		
		((Resetable) proxy).hardReset();
		
		assertEquals(JobState.READY, 
				((Stateful) proxy).lastStateEvent().getState());
		
		((Runnable) proxy).run();
		
		assertEquals(JobState.COMPLETE, 
				((Stateful) proxy).lastStateEvent().getState());
		
		assertEquals(3, r.ran);
	}
	
	public void testInOddjob() throws Exception {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <bean id='r' class='" + OurRunnable.class.getName() + "'/>" +
			"    <run id='j' job='${r}' />" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob); 
		
		assertEquals(1, lookup.lookup("r.ran"));
		
		RunJob test = lookup.lookup("j", RunJob.class);
		
		test.hardReset();
		
		test.run();
		
		assertEquals(1, lookup.lookup("r.ran"));
		
		oddjob.destroy();
	}
	
	public void testDestroyAfterRunning() throws InterruptedException, ArooaPropertyException, ArooaConversionException, ArooaParseException, FailedToStopException {
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <sequential>" +
				"   <jobs>" +
				"    <folder>" +
				"     <jobs>" +
				"    <wait id='w'/>" +
				"     </jobs>" +
				"    </folder>" +
				"    <run id='r' job='${w}' />" +
				"   </jobs>" +
				"  </sequential>" +
				" </job>" +
				"</oddjob>";
			
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Iconic wait = lookup.lookup("w", Iconic.class);
		
		IconSteps icons = new IconSteps(wait);

		icons.startCheck("ready", "executing", "sleeping");
				
		Thread t = new Thread(oddjob);
		t.start();
		
		icons.checkWait();
		
		((Stoppable) wait).stop();
		
		t.join();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		logger.info("*** Cutting.");
		
		DragPoint dp = oddjob.provideConfigurationSession(
				).dragPointFor(lookup.lookup("w"));
		
		DragTransaction trn = dp.beginChange(ChangeHow.FRESH);
		dp.cut();
		trn.commit();
		
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		logger.info("Destroying Oddjob.");
		
		oddjob.destroy();
	}
	
	private class MyStateful implements Stateful, Runnable {
				
		StateHandler<ServiceState> states = new StateHandler<ServiceState>(
				this, ServiceState.READY);

		
		void fireJobState(final ServiceState state) {
			try {
				states.tryToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						states.setState(state);
						states.fireEvent();
					}
				});
			}
			catch (OddjobLockedException e) {
				fail(e.getMessage());
			}
		}
		
		@Override
		public void addStateListener(StateListener listener) {
			states.addStateListener(listener);
		}
		
		@Override
		public void removeStateListener(StateListener listener) {
			states.removeStateListener(listener);
		}
		
		@Override
		public StateEvent lastStateEvent() {
			throw new RuntimeException("Unexpected");
		}
		
		@Override
		public void run() {
			fireJobState(ServiceState.STARTING);
		}
	}
	
	public void testDestroyedWhileActive() throws InterruptedException {

		MyStateful job = new MyStateful();
		
		RunJob test = new RunJob();
		test.setJob(job);
		
		IconSteps icons = new IconSteps(test);
		icons.startCheck(IconHelper.READY, IconHelper.EXECUTING, 
				IconHelper.SLEEPING);
		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.ACTIVE);

		Thread t = new Thread(test);
		t.start();
		
		icons.checkWait();
		icons.startCheck(IconHelper.SLEEPING, IconHelper.EXECUTING, 
				IconHelper.ACTIVE);
		
		job.fireJobState(ServiceState.STARTED);

		icons.checkWait();
		
		t.join();
		
		states.checkNow();
		states.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);
		
		job.fireJobState(ServiceState.DESTROYED);
		
		
		states.checkNow();
	}
	
	private class MyStateful2 extends MyStateful {
		
		@Override
		public void run() {
			fireJobState(ServiceState.STARTING);
			fireJobState(ServiceState.DESTROYED);
		}
	}
	
	public void testDestroyedWhileExecuting() throws InterruptedException {

		MyStateful2 job = new MyStateful2();
		
		RunJob test = new RunJob();
		test.setJob(job);
		
		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.EXCEPTION);

		test.run();
		
		states.checkNow();
		
		
	}
	
	public void testRunRemoteJob() 
	throws ArooaPropertyException, ArooaConversionException, 
			InterruptedException, FailedToStopException, PropertyVetoException {
		
		String xml =
				"<oddjob id='this' xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" +
				" <job>" +
				"  <sequential>" +
				"   <jobs>" +
				"    <folder>" +
				"     <jobs>" +
				"      <wait id='w'/>" +
				"     </jobs>" +
				"    </folder>" +
				"    <jmx:server id='server' root='${w}'/>" +
				"    <jmx:client id='client'/>" +
				"    <run id='r' job='${client/w}'/>" +
				"   </jobs>" +
				"  </sequential>" +
				" </job>" +
				"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Iconic test = lookup.lookup("r", Iconic.class);
		
		IconSteps icons = new IconSteps(test);

		icons.startCheck("ready", "executing", "sleeping");
		
		StateSteps states = new StateSteps(oddjob);

		states.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.ACTIVE);
		
		Thread t = new Thread(oddjob);
		t.start();
		
//		OddjobExplorer explorer = new OddjobExplorer();
//		explorer.setOddjob(oddjob);
//		explorer.run();
		
		icons.checkWait();
		
		logger.info("*** Stopping Wait *** ");
		
		Stoppable wait = lookup.lookup("w", Stoppable.class);
		wait.stop();
		
		t.join(5000);
		
		states.checkNow();
		
		states.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);
		
		logger.info("*** Stopping Oddjob *** ");
		
		oddjob.stop();
				
		states.checkNow();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		logger.info("*** Destroying *** ");
		
		oddjob.destroy();
	}
	
	public void testStopAndReset() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <sequential>" +
				"   <jobs>" +
				"    <folder>" +
				"     <jobs>" +
				"	   <sequential id='s'>" +
				"       <jobs>" +
				"    	 <wait id='w1'/>" +
				"    	 <wait id='w2'/>" +
				"       </jobs>" +
				"      </sequential>" +
				"     </jobs>" +
				"    </folder>" +
				"    <run id='r' job='${s}' />" +
				"   </jobs>" +
				"  </sequential>" +
				" </job>" +
				"</oddjob>";
			
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		// wait1
		
		WaitJob wait1 = lookup.lookup("w1", WaitJob.class);
		
		IconSteps icons1 = new IconSteps(wait1);

		icons1.startCheck("ready", "executing", "sleeping");
				
		Thread t = new Thread(oddjob);
		t.start();
		
		icons1.checkWait();
		
		icons1.startCheck("sleeping", "stopping", "complete");
		
		RunJob test = lookup.lookup("r", RunJob.class);
		
		test.stop();
		
		t.join(5000);
		
		icons1.checkNow();
		
		assertEquals(ParentState.READY, 
				oddjob.lastStateEvent().getState());
				
		// run again.
		// wait2
		
		WaitJob wait2 = lookup.lookup("w2", WaitJob.class);
		
		IconSteps icons2 = new IconSteps(wait2);

		icons2.startCheck("ready", "executing", "sleeping");
				
		t = new Thread(test);
		t.start();
		
		icons2.checkWait();
		
		icons2.startCheck("sleeping", "stopping", "complete");
		
		wait2.stop();
		
		t.join(5000);
		
		icons2.checkNow();
		
		assertEquals(ParentState.COMPLETE, 
				test.lastStateEvent().getState());
				
		// reset
		
		test.hardReset();
		
		assertEquals(ParentState.READY, 
				test.lastStateEvent().getState());

		SequentialJob sequential = lookup.lookup("s", SequentialJob.class);
		
		assertEquals(ParentState.COMPLETE, 
				sequential.lastStateEvent().getState());
		
		logger.info("Destroying Oddjob.");
		
		oddjob.destroy();
	}
}
