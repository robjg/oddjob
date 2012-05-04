/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import junit.framework.TestCase;

import org.apache.commons.beanutils.PropertyUtils;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.MockComponentPool;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeListener;
import org.oddjob.arooa.standard.StandardTools;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;

public class ServiceWrapperTest extends TestCase {

	class OurContext extends MockArooaContext {
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
	
	class OurSession extends MockArooaSession {
		Object configured;
		Object saved;
		
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
		
		ServiceAdaptor service = new ServiceStrategies().serviceFor(
				myService);
		
		OurSession session = new OurSession();
		
		OurContext context = new OurContext();
		context.session = session;
		
		Runnable wrapper = (Runnable) new ServiceProxyGenerator().generate(
				service, getClass().getClassLoader());

		((ArooaContextAware) wrapper).setArooaContext(context);
		
		wrapper.run();

		assertEquals(wrapper, session.configured);
		
		assertEquals(ServiceState.STARTED, Helper.getJobState(wrapper));
		assertEquals(new Boolean(true), PropertyUtils.getProperty(wrapper, "started"));
		
		((Stoppable) wrapper).stop();

		assertEquals(ServiceState.COMPLETE, Helper.getJobState(wrapper));
		assertEquals(new Boolean(true), PropertyUtils.getProperty(wrapper, "stopped"));

		((Resetable) wrapper).hardReset();

		// Service don't persist.
		assertNull(session.saved);
		
		assertEquals(ServiceState.READY, Helper.getJobState(wrapper));
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
    	
    	Object r = new OddjobLookup(oj).lookup("s");
    	assertEquals(ServiceState.STARTED, Helper.getJobState(r));
    	assertEquals(new Boolean(true), PropertyUtils.getProperty(r, "started"));
    	
    	oj.stop();
    	
    	assertEquals(ServiceState.COMPLETE, Helper.getJobState(r));
    	assertEquals(new Boolean(true), PropertyUtils.getProperty(r, "stopped"));    	
    }
    
    public static class Bean {
    	String greeting;
    	public void setGreeting(String greeting) {
    		this.greeting = greeting;
    	}
    	public String getGreeting() {
    		return greeting;
    	}
    }
    
   public static class MyS2 {
    	public String result;
    	public void start() {	
    	}
    	public void stop() {
    	}
    	public void setResult(String result) {
    		this.result = result;
    	}
    	public String getResult() {
    		return result;
    	}
    }    
    
    public void testInOddjob2() throws Exception {
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <sequential>" +
    		"   <jobs>" +
    		"    <variables id='v'>" +
    		"     <x>" +
    		"      <value value='0'/>" +
    		"     </x>" +
    		"    </variables>" +
    		"    <bean class='" + MyS2.class.getName() + "' " +
    		"           id='s' result='${v.x}' />" +
    		"   </jobs>" +
    		"  </sequential>" +
    		" </job>" +
    		"</oddjob>";
    	
    	Oddjob oj = new Oddjob();
    	oj.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	oj.run();
    	
    	assertEquals(ParentState.ACTIVE, oj.lastStateEvent().getState());
    	
    	Object r = new OddjobLookup(oj).lookup("s");
    	assertEquals("0", PropertyUtils.getProperty(r, "result"));
    	
    	oj.stop();    	
    	
    	assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
    }
}
