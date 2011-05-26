package org.oddjob.state;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.MockStateful;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.StateSteps;
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

public class MirrorStateTest extends TestCase {

	private static final Logger logger = Logger.getLogger(MirrorStateTest.class);
	
	@Override
	protected void setUp() throws Exception {
		logger.info("-----------------   " + getName() + "  ---------------");
	}
	
	private class Result implements JobStateListener {
		JobState result;
		
		public void jobStateChange(JobStateEvent event) {
			result = event.getJobState();
		}
	}
	
	private class Icon implements IconListener {
		
		String iconId;
		
		public void iconEvent(IconEvent e) {
			iconId = e.getIconId();
		}
	}
	
	
	public void testComplete() {
		
		MirrorState test = new MirrorState();
		
		Result listener = new Result();
		Icon icon = new Icon();
		
		test.addJobStateListener(listener);
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
				job.lastJobStateEvent().getJobState());
		
		assertEquals(JobState.COMPLETE, listener.result);		

		test.hardReset();
		
		test.setJob(job);
		test.run();
		
		assertEquals(JobState.READY, listener.result);
	}	
	
	public void testReset() {
		
		MirrorState test = new MirrorState();
		
		FlagState job = new FlagState(JobState.COMPLETE);
		test.setJob(job);

		job.run();
		
		assertEquals(JobState.READY, 
				test.lastJobStateEvent().getJobState());

		test.run();
		
		assertEquals(JobState.COMPLETE, 
				test.lastJobStateEvent().getJobState());
		
		assertEquals(JobState.COMPLETE, 
				test.lastJobStateEvent().getJobState());
		
		test.softReset();
		
		assertEquals(JobState.READY, 
				test.lastJobStateEvent().getJobState());

		test.setJob(job);
		test.run();
		
		assertEquals(JobState.COMPLETE, 
				test.lastJobStateEvent().getJobState());
		
		test.stop();
		
		assertEquals(JobState.COMPLETE, 
				test.lastJobStateEvent().getJobState());
	}

	private class ExecutingThing extends MockStateful {

		JobStateListener listener;
		
		@Override
		public void addJobStateListener(JobStateListener listener) {
			assertNull(this.listener);
			assertNotNull(listener);
			this.listener = listener;
		}
		@Override
		public void removeJobStateListener(JobStateListener listener) {
			assertNotNull(this.listener);
			assertEquals(listener, this.listener);
			this.listener = null;
		}
	}
	
	public void testStop() {
		
		MirrorState test = new MirrorState();
		
		ExecutingThing job = new ExecutingThing();
		test.setJob(job);

		assertEquals(JobState.READY, 
				test.lastJobStateEvent().getJobState());

		test.run();
		
		assertEquals(JobState.READY, 
				test.lastJobStateEvent().getJobState());
		
		job.listener.jobStateChange(new JobStateEvent(job, 
				JobState.EXECUTING));
				
		assertEquals(JobState.EXECUTING, 
				test.lastJobStateEvent().getJobState());
		
		assertEquals(JobState.EXECUTING, 
				test.lastJobStateEvent().getJobState());
		
		test.stop();
		
		assertEquals(JobState.READY, 
				test.lastJobStateEvent().getJobState());
		
		assertNull(job.listener);
	}
	
	private class OurStateful extends MockStateful {
		JobStateHandler state = new JobStateHandler(this);
		
		public void addJobStateListener(JobStateListener listener) {
			state.addJobStateListener(listener);
		}
		public void removeJobStateListener(JobStateListener listener) {
			state.removeJobStateListener(listener);
		}
		
		void startRunning() {
			state.waitToWhen(new StateCondition() {
				public boolean test(JobState state) {
					return true;
				}
			}, new Runnable() {
				public void run() {
					state.setJobState(JobState.EXECUTING);
					state.fireEvent();
				}
			});
		}
		
		void destroy() {
			state.waitToWhen(new StateCondition() {
				public boolean test(JobState state) {
					return true;
				}
			}, new Runnable() {
				public void run() {
					state.setJobState(JobState.DESTROYED);
					state.fireEvent();
				}
			});
		}
	}
	
	public void testDestroyed() {
		
		MirrorState test = new MirrorState();
		
		OurStateful job = new OurStateful();
		test.setJob(job);

		test.run();
		
		job.destroy();
		
		assertEquals(JobState.EXCEPTION, 
				test.lastJobStateEvent().getJobState());

	}
	
	public void testInOddjob() {
		
		String xml = 
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:and>" +
			"   <jobs>" +
			"    <echo id='e' text='hello'/>" +
			"    <state:mirror job='${e}'/>" +
			"   </jobs>" +
			"  </state:and>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		assertEquals(JobState.COMPLETE, oddjob.lastJobStateEvent().getJobState());	

		oddjob.destroy();
	}
	
	public void testMirroredJobCut() throws ArooaParseException {
		
		String xml = 
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <folder>" +
			"     <jobs>" +
			"      <echo id='e' text='hello'/>" +
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
		
		assertEquals(JobState.READY, oddjob.lastJobStateEvent().getJobState());	

		Object on = new OddjobLookup(oddjob).lookup("e");
		
		DragPoint drag = oddjob.provideConfigurationSession().dragPointFor(on);
		DragTransaction trn = drag.beginChange(ChangeHow.FRESH);
		drag.cut();
		try {
			trn.commit();
		} catch (ArooaParseException e) {
			trn.rollback();
			throw e;
		}
		
		assertEquals(JobState.EXCEPTION, oddjob.lastJobStateEvent().getJobState());	
		
		oddjob.destroy();
		
		services.stop();
		
		assertEquals(JobState.DESTROYED, oddjob.lastJobStateEvent().getJobState());	
	}
	
	public void testStopWhenRunning() {

		OurStateful running = new OurStateful();
		running.startRunning();
		
		MirrorState test = new MirrorState();
		
		test.setJob(running);
		test.run();
	
		assertEquals(JobState.EXECUTING, 
				test.lastJobStateEvent().getJobState());
		
		test.stop();
		
		assertEquals(JobState.READY, 
				test.lastJobStateEvent().getJobState());

	}
	
	public void testStopWhenRunningInOddjob() {

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

		StateSteps steps = new StateSteps(oddjob);
		
		steps.startCheck(JobState.READY, JobState.EXECUTING);

		oddjob.run();
		
		steps.checkNow();
		steps.startCheck(JobState.EXECUTING, JobState.READY, JobState.DESTROYED);
		
		oddjob.destroy();
		
		steps.checkNow();
	}
}
