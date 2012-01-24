package org.oddjob.framework;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.IconSteps;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.StateSteps;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.images.IconHelper;
import org.oddjob.state.JobState;

public class SimpleJobTest extends TestCase {

	
	public static class OurJob extends SimpleJob {
		
		List<String> results = new ArrayList<String>();
		
		public void setConstantAttribute(String value) {
			results.add("Set constant attribute: " + value);
		}
		
		public void setRuntimeAttribute(String value) {
			results.add("Set runtime attribute: " + value);			
		}
		
		public void setElementProperty(Object value) {
			results.add("Set element property: " + value);
		}
		
		@Inject
		public void setClassLoader(ClassLoader classLoader) {
			results.add("Set auto property: " + 
					(classLoader == null ? "null" : "ClassLoader"));
			
		}
		
		@Override
		protected int execute() throws Throwable {
			results.add("Executing.");
			return 0;
		}
		
		@Override
		protected void onInitialised() {
			super.onInitialised();
			results.add("In onInitialised.");
		}
		
		@Override
		protected void onDestroy() {
			results.add("In onDestroy.");
		}
				
		@Override
		protected void onConfigured() {
			super.onConfigured();
			results.add("In onConfigured.");
		}
		
		public String getSomeValue() {
			return "Orange";
		}
		
		public List<String> getResults() {
			return results;
		}
	}
	
	
	public void testFullLifeCycleInOddjob() throws ArooaPropertyException, ArooaConversionException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean id='ours'" +
			"         class='" + OurJob.class.getName() + "'" +
			"         constantAttribute='Apple'" +
			"         runtimeAttribute='${ours.someValue}'>" +
			"    <elementProperty>" +
			"       <value value='Banana'/>" +
			"    </elementProperty>" +
			"  </bean>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
			
		@SuppressWarnings("unchecked")
		List<String> results = (List<String>) new OddjobLookup(oddjob).lookup("ours.results", 
				List.class);
		
		assertEquals("Set constant attribute: Apple", results.get(0));
		assertEquals("In onInitialised.", results.get(1));
		assertEquals("Set element property: Banana", results.get(2));
		assertEquals("Set runtime attribute: Orange", results.get(3));
		assertEquals("Set auto property: ClassLoader", results.get(4));
		assertEquals("In onConfigured.", results.get(5));
		assertEquals("Executing.", results.get(6));
		
		assertEquals(7, results.size());
		
		oddjob.destroy();
				
		assertEquals("In onDestroy.", results.get(7));
		assertEquals("Set element property: null", results.get(8));
		
		assertEquals(9, results.size());
	}
	
	private class SleepyJob extends SimpleJob {
		
		long sleep;
		
		@Override
		protected int execute() throws Throwable {
			sleep(sleep);
			
			return 0;
		}
	}
		
	public void testSleepAndComplete() throws InterruptedException, FailedToStopException {
		
		SleepyJob test = new SleepyJob();
		test.sleep = 1;
		IconSteps icons = new IconSteps(test);
		icons.startCheck(IconHelper.READY, IconHelper.EXECUTING, 
				IconHelper.SLEEPING, IconHelper.EXECUTING,
				IconHelper.COMPLETE);
		
		StateSteps state = new StateSteps(test);		
		state.startCheck(JobState.READY, JobState.EXECUTING,
				JobState.COMPLETE);
		
		test.run();

		state.checkNow();
		icons.checkNow();
	}
	
	public void testSleepAndStop() throws InterruptedException, FailedToStopException {
		
		SleepyJob test = new SleepyJob();

		IconSteps icons = new IconSteps(test);
		icons.startCheck(IconHelper.READY, IconHelper.EXECUTING, 
				IconHelper.SLEEPING);
		
		StateSteps state = new StateSteps(test);		
		state.startCheck(JobState.READY, JobState.EXECUTING);
		
		new Thread(test).start();

		state.checkWait();
		icons.checkWait();

		icons.startCheck(IconHelper.SLEEPING,
				IconHelper.STOPPING,
				IconHelper.COMPLETE);
		
		test.stop();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());

		icons.checkNow();
	}
	
	public void testForceable() {
		
		SimpleJob test = new SimpleJob() {
			
			@Override
			protected int execute() throws Throwable {
				return 0;
			}
		};
		
		assertEquals(JobState.READY, test.lastStateEvent().getState());
		
		test.force();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
	}
}
