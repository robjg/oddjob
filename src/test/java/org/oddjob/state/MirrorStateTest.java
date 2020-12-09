package org.oddjob.state;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MirrorStateTest extends OjTestCase {

	private static final Logger logger = LoggerFactory.getLogger(MirrorStateTest.class);
	
    @Before
    public void setUp() throws Exception {
		logger.info("-----------------   " + getName() + "  ---------------");
	}
	
	private class Result implements StateListener {
		JobState result;
		
		public void jobStateChange(StateEvent event) {
			result = (JobState) event.getState();
		}
	}
	
	private class Icon implements IconListener {
		
		String iconId;
		
		public void iconEvent(IconEvent e) {
			iconId = e.getIconId();
		}
	}
	
	
   @Test
	public void testComplete() {
		
		MirrorState test = new MirrorState();
		
		Result listener = new Result();
		Icon icon = new Icon();
		
		test.addStateListener(listener);
		test.addIconListener(icon);
		
		assertEquals(JobState.READY, listener.result);
		assertEquals(IconHelper.READY, icon.iconId);
		
		FlagState job = new FlagState(JobState.COMPLETE);

		test.setJob(job);
		
		assertEquals(JobState.READY, listener.result);

		test.run();
		
		assertEquals(JobState.READY, listener.result);
		
		job.run();
		
		assertEquals(JobState.COMPLETE, listener.result);
		assertEquals(IconHelper.COMPLETE, icon.iconId);
		
		test.stop();
		
		job.hardReset();
		
		assertEquals(JobState.READY, 
				job.lastStateEvent().getState());
		
		assertEquals(JobState.COMPLETE, listener.result);		

		test.hardReset();
		
		test.setJob(job);
		test.run();
		
		assertEquals(JobState.READY, listener.result);
	}	
	
   @Test
	public void testReset() {
		
		MirrorState test = new MirrorState();
		
		FlagState job = new FlagState(JobState.COMPLETE);
		test.setJob(job);

		job.run();
		
		assertEquals(JobState.READY, 
				test.lastStateEvent().getState());

		test.run();
		
		assertEquals(JobState.COMPLETE, 
				test.lastStateEvent().getState());
		
		assertEquals(JobState.COMPLETE, 
				test.lastStateEvent().getState());
		
		test.softReset();
		
		assertEquals(JobState.READY, 
				test.lastStateEvent().getState());

		test.setJob(job);
		test.run();
		
		assertEquals(JobState.COMPLETE, 
				test.lastStateEvent().getState());
		
		test.stop();
		
		assertEquals(JobState.COMPLETE, 
				test.lastStateEvent().getState());
	}

	private class ExecutingThing extends MockStateful {

		StateListener listener;
		
		@Override
		public void addStateListener(StateListener listener) {
			assertNull(this.listener);
			assertNotNull(listener);
			this.listener = listener;
		}
		@Override
		public void removeStateListener(StateListener listener) {
			assertNotNull(this.listener);
			assertEquals(listener, this.listener);
			this.listener = null;
		}
	}
	
   @Test
	public void testStop() {
		
		MirrorState test = new MirrorState();
		
		ExecutingThing job = new ExecutingThing();
		test.setJob(job);

		assertEquals(JobState.READY, 
				test.lastStateEvent().getState());

		test.run();
		
		assertEquals(JobState.READY, 
				test.lastStateEvent().getState());
		
		job.listener.jobStateChange(new StateEvent(job, 
				JobState.EXECUTING));
				
		assertEquals(JobState.EXECUTING, 
				test.lastStateEvent().getState());
		
		assertEquals(JobState.EXECUTING, 
				test.lastStateEvent().getState());
		
		test.stop();
		
		assertEquals(JobState.READY, 
				test.lastStateEvent().getState());
		
		assertNull(job.listener);
	}
	
	private class OurStateful extends MockStateful {
		JobStateHandler state = new JobStateHandler(this);
		
		public void addStateListener(StateListener listener) {
			state.addStateListener(listener);
		}
		public void removeStateListener(StateListener listener) {
			state.removeStateListener(listener);
		}
		
		void startRunning() {
			state.waitToWhen(new IsAnyState(), new Runnable() {
				public void run() {
					state.setState(JobState.EXECUTING);
					state.fireEvent();
				}
			});
		}
		
		void destroy() {
			state.waitToWhen(new IsAnyState(), new Runnable() {
				public void run() {
					state.setState(JobState.DESTROYED);
					state.fireEvent();
				}
			});
		}
	}
	
   @Test
	public void testDestroyed() {
		
		MirrorState test = new MirrorState();
		
		OurStateful job = new OurStateful();
		test.setJob(job);

		test.run();
		
		job.destroy();
		
		assertEquals(JobState.EXCEPTION, 
				test.lastStateEvent().getState());

	}
	
   @Test
	public void testInOddjob() throws InterruptedException {
		
		String xml = 
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:and>" +
			"   <jobs>" +
			"    <echo id='e'>hello</echo>" +
			"    <state:mirror job='${e}'/>" +
			"   </jobs>" +
			"  </state:and>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		StateSteps state = new StateSteps(oddjob);
		state.startCheck(ParentState.READY, 
				ParentState.EXECUTING,
				ParentState.COMPLETE);
		
		oddjob.run();
		
		state.checkWait();	

		oddjob.destroy();
	}
	
   @Test
	public void testMirroredJobCut() throws ArooaParseException {
		
		String xml = 
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <folder>" +
			"     <jobs>" +
			"      <echo id='e'>hello</echo>" +
			"     </jobs>" +
			"    </folder>" +
			"    <state:mirror job='${e}'/>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		DefaultExecutors services  = new DefaultExecutors(); 
		
		Oddjob oddjob = new Oddjob();
		oddjob.setOddjobExecutors(services);
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());	

		Object on = new OddjobLookup(oddjob).lookup("e");
		
		DragPoint drag = oddjob.provideConfigurationSession().dragPointFor(on);
		DragTransaction trn = drag.beginChange(ChangeHow.FRESH);
		drag.delete();
		try {
			trn.commit();
		} catch (ArooaParseException e) {
			trn.rollback();
			throw e;
		}
		
		assertEquals(ParentState.EXCEPTION, oddjob.lastStateEvent().getState());	
		
		oddjob.destroy();
		
		services.stop();
		
		assertEquals(ParentState.DESTROYED, oddjob.lastStateEvent().getState());	
	}
	
   @Test
	public void testStopWhenRunning() {

		OurStateful running = new OurStateful();
		running.startRunning();
		
		MirrorState test = new MirrorState();
		
		test.setJob(running);
		test.run();
	
		assertEquals(JobState.EXECUTING, 
				test.lastStateEvent().getState());
		
		test.stop();
		
		assertEquals(JobState.READY, 
				test.lastStateEvent().getState());

	}
	
   @Test
	public void testStopWhenRunningInOddjob() throws FailedToStopException {

		OurStateful running = new OurStateful();
		running.startRunning();
		
		String xml = 
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:mirror job='${job}'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.setExport("job", new ArooaObject(running));

		StateSteps oddjobStates = new StateSteps(oddjob);
		
		oddjobStates.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE);

		oddjob.run();
		
		oddjobStates.checkNow();
		oddjobStates.startCheck(ParentState.ACTIVE, ParentState.READY);
		
		oddjob.stop();
		
		oddjobStates.checkNow();
		
		oddjobStates.startCheck(ParentState.READY, 
				ParentState.DESTROYED);
		
		oddjob.destroy();
		
		oddjobStates.checkNow();
	}
}
