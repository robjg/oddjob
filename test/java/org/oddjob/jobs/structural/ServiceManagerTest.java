package org.oddjob.jobs.structural;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConsoleCapture;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.Service;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.state.StateEvent;

public class ServiceManagerTest extends TestCase {

	private static final Logger logger = Logger.getLogger(ServiceManagerTest.class);
	
	public static class Lights implements Service {
		
		volatile String are = "off";
		
		@Override
		public void start() throws Exception {
			Thread.sleep(10);
			are = "on";
		}
		
		@Override
		public void stop() throws FailedToStopException {
			are = "off";
		}
		
		public String getAre() {
			return are;
		}
		
		@Override
		public String toString() {
			return "Light Service";
		}
	}

	public static class MachineThatGoes implements Service {
		
		volatile String goes;
		volatile String reallyGoes;
		
		@Override
		public void start() throws Exception {
			// Try to mess up threading a bit.
			Thread.sleep(20);
			reallyGoes = goes;
		}
		
		@Override
		public void stop() throws FailedToStopException {
			goes = "nothing";
		}
		
		public void setGoes(String goes) {
			this.goes = goes;
		}
		
		public String getGoes() {
			return reallyGoes;
		}
		
		@Override
		public String toString() {
			return "The Machine";
		}
	}
	
	public static class MachineThatBreaks implements Service {
		
		@Override
		public void start() throws Exception {
			throw new UnsupportedOperationException(
					"This machine will never work!");
		}
		
		@Override
		public void stop() throws FailedToStopException {
		}
				
		@Override
		public String toString() {
			return "Broken Machine";
		}
	}
	
	public void testExample() throws FailedToStopException, ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/structural/ServiceManagerExample.xml", 
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		StateSteps steps = new StateSteps(oddjob);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING,
				ParentState.COMPLETE);		
		
		oddjob.run();		
		
		steps.checkNow();
		
		console.close();
		
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals(1, lines.length);
				
		assertEquals("The lights are on and the machine goes ping.", lines[0].trim());
				
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		ServiceManager test = lookup.lookup("service-manager", 
				ServiceManager.class);
		Object lights = lookup.lookup("lights");
		Object machine = lookup.lookup("machine");
		
		StateEvent testState = test.lastStateEvent();
		assertEquals(ParentState.COMPLETE, testState.getState());
		
		StateSteps lightsState = new StateSteps((Stateful) lights);
		lightsState.startCheck(ServiceState.STARTED, ServiceState.COMPLETE);
		
		StateSteps machineState = new StateSteps((Stateful) machine);
		machineState.startCheck(ServiceState.STARTED, ServiceState.COMPLETE);
		
		oddjob.stop();
		
		lightsState.checkNow();
		machineState.checkNow();
		
		assertEquals(testState, test.lastStateEvent());
		
		lightsState.startCheck(ServiceState.COMPLETE, ServiceState.READY);
		
		((Resetable) lights).hardReset();

		lightsState.checkNow();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		
		lightsState.startCheck(ServiceState.READY, 
				ServiceState.STARTING, ServiceState.STARTED);
		
		((Runnable) lights).run();
		
		lightsState.checkNow();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		oddjob.destroy();	
	}
	
	public void testException() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/structural/ServiceManagerBroken.xml", 
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		StateSteps steps = new StateSteps(oddjob);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING,
				ParentState.EXCEPTION);		
		
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		ServiceManager test = lookup.lookup("service-manager", 
				ServiceManager.class);
		
		StateSteps testState = new StateSteps(test);
		testState.startCheck(ParentState.READY, 
				ParentState.EXECUTING,
				ParentState.EXCEPTION);
		
		oddjob.run();		
		
		steps.checkNow();
		
		console.close();
		
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals(0, lines.length);
								
		Object lights = lookup.lookup("lights");
		Object machine = lookup.lookup("machine");
		
		testState.checkNow();

		StateEvent lightsState = ((Stateful) lights).lastStateEvent();
		assertEquals(ServiceState.STARTED, lightsState.getState());
		
		StateSteps machineState = new StateSteps((Stateful) machine);
		machineState.startCheck(ServiceState.EXCEPTION, ServiceState.READY, 
				ServiceState.STARTING, ServiceState.EXCEPTION);
		testState.startCheck(ParentState.EXCEPTION,
				ParentState.READY, 
				ParentState.EXECUTING,
				ParentState.EXCEPTION);
		
		((Resetable) machine).softReset();
		test.run();
		
		machineState.checkNow();
		testState.checkNow();
		
		oddjob.destroy();	
	}
	
	public void testOneJob() throws ArooaPropertyException, ArooaConversionException, FailedToStopException {
		
		String xml =
				"<oddjob>" +
				" <job>" +
				"  <serviceman id='service-manager'>" +
				"   <jobs>" +
				"    <bean id='lights' " +
				"       class='org.oddjob.jobs.structural.ServiceManagerTest$Lights'/>" +
				"   </jobs>" +
				"  </serviceman>" +
				" </job>" +
				"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("TEST", xml));
		
		StateSteps steps = new StateSteps(oddjob);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING,
				ParentState.COMPLETE);		
		
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		ServiceManager test = lookup.lookup("service-manager", 
				ServiceManager.class);
		
		StateSteps testState = new StateSteps(test);
		testState.startCheck(ParentState.READY, 
				ParentState.EXECUTING,
				ParentState.COMPLETE);
		
		oddjob.run();		
		
		steps.checkNow();
		testState.checkNow();
										
		Object lights = lookup.lookup("lights");

		assertEquals(ServiceState.STARTED, 
				((Stateful) lights).lastStateEvent().getState());
		
		testState.startCheck(ParentState.COMPLETE,
				ParentState.READY);
		
		test.stop();
		
		assertEquals(ServiceState.COMPLETE, 
				((Stateful) lights).lastStateEvent().getState());
		
		((Resetable) lights).hardReset();
		
		assertEquals(ServiceState.READY, 
				((Stateful) lights).lastStateEvent().getState());
		
		testState.checkNow();
		
		testState.startCheck(ParentState.READY,
				ParentState.EXECUTING,
				ParentState.COMPLETE);
		
		test.run();
		
		assertEquals(ServiceState.STARTED, 
				((Stateful) lights).lastStateEvent().getState());
		
		testState.checkNow();
		
		oddjob.destroy();	
	}
	
	public void testParallelExample() throws FailedToStopException, ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/structural/ServiceManagerParallel.xml", 
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		StateSteps oddjobStates = new StateSteps(oddjob);
		oddjobStates.startCheck(ParentState.READY, 
				ParentState.EXECUTING,
				ParentState.ACTIVE,
				ParentState.COMPLETE);		
		
		oddjob.run();		
		
		oddjobStates.checkWait();
		
		console.close();
		
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals(1, lines.length);
				
		assertEquals("The lights are on and the machine goes ping.", lines[0].trim());
				
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		ServiceManager test = lookup.lookup("service-manager", 
				ServiceManager.class);
		Object lights = lookup.lookup("lights");
		Object machine = lookup.lookup("machine");
		
		StateEvent testState = test.lastStateEvent();
		assertEquals(ParentState.COMPLETE, testState.getState());
		
		StateSteps lightsState = new StateSteps((Stateful) lights);
		lightsState.startCheck(ServiceState.STARTED, ServiceState.COMPLETE);
		
		StateSteps machineState = new StateSteps((Stateful) machine);
		machineState.startCheck(ServiceState.STARTED, ServiceState.COMPLETE);
		
		oddjob.stop();
		
		lightsState.checkNow();
		machineState.checkNow();
		
		assertEquals(testState, test.lastStateEvent());
		
		lightsState.startCheck(ServiceState.COMPLETE, ServiceState.READY);
		
		((Resetable) lights).hardReset();

		lightsState.checkNow();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		
		lightsState.startCheck(ServiceState.READY, 
				ServiceState.STARTING, ServiceState.STARTED);
		
		((Runnable) lights).run();
		
		lightsState.checkNow();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		oddjob.destroy();	
	}
}
