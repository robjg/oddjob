/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import java.beans.ExceptionListener;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.oddjob.Describeable;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.MockComponentPool;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeListener;
import org.oddjob.arooa.standard.StandardArooaDescriptor;
import org.oddjob.arooa.standard.StandardTools;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;

public class ServiceWrapperTest extends TestCase {

	private static final Logger logger = Logger.getLogger(ServiceWrapperTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("-------------------------  " + getName() + 
				"  ------------------------");
	}
	
	private class OurContext extends MockArooaContext {
		OurSession session;
		
		@Override
		public ArooaSession getSession() {
			return session;
		}
		
		@Override
		public RuntimeConfiguration getRuntime() {
			return new MockRuntimeConfiguration() {
				@Override
				public void addRuntimeListener(RuntimeListener listener) {
				}
			};
		}
	}
	
	private class OurSession extends MockArooaSession {
		Object configured;
		Object saved;
		
		ArooaDescriptor descriptor = new StandardArooaDescriptor();
		
		@Override
		public ArooaDescriptor getArooaDescriptor() {
			return descriptor;
		}
		
		@Override
		public ComponentPool getComponentPool() {
			return new MockComponentPool() {
				@Override
				public void configure(Object component) {
					configured = component;
				}
				@Override
				public void save(Object component) {
					saved = component;
				}
			};
		}
		
		@Override
		public ArooaTools getTools() {
			return new StandardTools();
		}
	}
	
	public static class MyService {
		boolean started;
		boolean stopped;
		
		public void start() {
			started = true;
		}
		public void stop() {
			stopped = true;
		}
		public boolean isStarted() {
			return started;
		}
		public boolean isStopped() {
			return stopped;
		}
	}
	
	public void testStartStop() throws Exception {
		MyService myService = new MyService();
		
		OurSession session = new OurSession();
		
		ServiceAdaptor service = new ServiceStrategies().serviceFor(
				myService, session);
		
		OurContext context = new OurContext();
		context.session = session;
		
		Runnable wrapper = (Runnable) new ServiceProxyGenerator().generate(
				service, getClass().getClassLoader());

		((ArooaSessionAware) wrapper).setArooaSession(session);
		((ArooaContextAware) wrapper).setArooaContext(context);
		
		wrapper.run();

		assertEquals(wrapper, session.configured);
		
		assertEquals(ServiceState.STARTED, OddjobTestHelper.getJobState(wrapper));
		assertEquals(new Boolean(true), PropertyUtils.getProperty(wrapper, "started"));
		
		((Stoppable) wrapper).stop();

		assertEquals(ServiceState.COMPLETE, OddjobTestHelper.getJobState(wrapper));
		assertEquals(new Boolean(true), PropertyUtils.getProperty(wrapper, "stopped"));

		((Resetable) wrapper).hardReset();

		// Service don't persist.
		assertNull(session.saved);
		
		assertEquals(ServiceState.READY, OddjobTestHelper.getJobState(wrapper));
	}

    public void testInOddjob() throws Exception {
    	String xml = "<oddjob>" +
    			" <job>" +
    			"  <bean class='" + MyService.class.getName() + "' id='s' />" +
    			" </job>" +
    			"</oddjob>";

    	Oddjob oj = new Oddjob();
    	oj.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	oj.run();
    	
    	Object test = new OddjobLookup(oj).lookup("s");
    	assertEquals(ServiceState.STARTED, OddjobTestHelper.getJobState(test));
    	assertEquals(new Boolean(true), 
    			PropertyUtils.getProperty(test, "started"));
    	
    	oj.stop();
    	
    	assertEquals(ServiceState.COMPLETE, OddjobTestHelper.getJobState(test));
    	assertEquals(new Boolean(true), 
    			PropertyUtils.getProperty(test, "stopped"));

    	Map<String, String> description = ((Describeable) test).describe();
    	assertEquals("true", description.get("started"));
    	assertEquals("true", description.get("stopped"));
    	
    	oj.destroy();
    }
        
    public interface FruitService {
    	String getFruit();
    }
    
    public static class FruitJob implements Runnable {
    	private FruitService fruitService;
    	
    	public void setFruitService(FruitService service) {
    		this.fruitService = service;
    	}
    	
    	@Override
    	public void run() {
    		fruitService.getFruit();
    	}
    }
    
    public static class MyFallibleService implements FruitService {

    	ExceptionListener exceptionListener;
    	
    	@Start
    	public void myStart() {	
    		
    	}
    	
    	@Stop
    	public void myStop() {
    		
    	}
    	
    	@AcceptExceptionListener
    	public void handler(ExceptionListener exceptionListener) {
    		this.exceptionListener = exceptionListener;
    	}
    	
    	@Override
    	public String getFruit() {
    		exceptionListener.exceptionThrown(new Exception("No More Fruit!"));
    		return "Apple";
    	}
    }    

    public void testExceptionCallbackInOddjob() throws Exception {
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <sequential>" +
    		"   <jobs>" +
    		"    <bean id='fruit-service' class='" + MyFallibleService.class.getName() + "'/>" +
    		"    <bean id='fruit-job' class='" + FruitJob.class.getName() + "'>" +
    		"     <fruitService>" +
    		"      <value value='${fruit-service}'/>" +
    		"     </fruitService>" +
    		"    </bean>" +
    		"   </jobs>" +
    		"  </sequential>" +
    		" </job>" +
    		"</oddjob>";
     	
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	oddjob.load();
    	
    	OddjobLookup lookup = new OddjobLookup(oddjob);
    	
    	Stateful service = lookup.lookup("fruit-service", Stateful.class);
    	    	
    	StateSteps serviceStates = new StateSteps(service);
    	
    	serviceStates.startCheck(ServiceState.READY, ServiceState.STARTING, 
    			ServiceState.STARTED, ServiceState.EXCEPTION);
    	
    	oddjob.run();
    	
    	serviceStates.checkNow();
    	
    	Stateful job = lookup.lookup("fruit-job", Stateful.class);
    	
    	assertEquals(JobState.COMPLETE, 
    			job.lastStateEvent().getState());
    	
    	assertEquals(ParentState.EXCEPTION, 
    			oddjob.lastStateEvent().getState());
    	
    	oddjob.destroy();    	
    }
}
