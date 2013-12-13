/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.oddjob.Describeable;
import org.oddjob.FailedToStopException;
import org.oddjob.Forceable;
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
import org.oddjob.arooa.life.ArooaLifeAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.MockComponentPool;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeListener;
import org.oddjob.arooa.standard.StandardArooaDescriptor;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.standard.StandardTools;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.log4j.Log4jArchiver;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;

/**
 *
 * @author Rob Gordon.
 */
public class RunnableWrapperTest extends TestCase {

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
	
	
	/** Runnable fixture. */
	public static class OurRunnable implements Runnable {
		boolean ran;
        public void run() {
        	ran = true;
        }
        public boolean isRan() {
        	return ran;
        }
        public String toString() {
        	return "OurRunnable";
        }
	}
	
	/** 
	 * Test a runable that runs without exception. Test the 
	 * state of the wrapper.
	 *
	 */
    public void testGoodRunnable() {
    	OurSession session = new OurSession();
    	
    	OurContext context = new OurContext();
    	context.session = session;
    	
    	OurRunnable test = new OurRunnable();
    	
        Object proxy = new RunnableProxyGenerator().generate(
    			(Runnable) test,
    			getClass().getClassLoader());  
        
        ((ArooaSessionAware) proxy).setArooaSession(session);
        ((ArooaContextAware) proxy).setArooaContext(context);
        
        MyStateListener stateListener = new MyStateListener();
        ((Stateful) proxy).addStateListener(stateListener);
        
        assertSame(proxy, stateListener.lastEvent.getSource());
        
        ((Runnable) proxy).run();
        
        assertEquals(proxy, session.configured);
        assertEquals(proxy, session.saved);
        
        assertTrue(test.ran);
        assertEquals("JobState", JobState.COMPLETE, 
                stateListener.lastEvent.getState());
        
        session.saved = null;
        
        ((Resetable) proxy).hardReset();
        assertEquals("JobState", JobState.READY, 
                stateListener.lastEvent.getState());
	
        assertEquals(proxy, session.saved);
        
        ((Forceable) proxy).force();
        
        assertEquals("JobState", JobState.COMPLETE, 
                stateListener.lastEvent.getState());
    }

    /**
     * Test a runnable that throws an exception. Test the 
     * state of the wrapper.
     *
     */
    public void testBadRunnable() {
    	
    	Runnable test = new Runnable() {
            public void run() {
                throw new RuntimeException("How bad is this!");
            }
        };
        
        Object proxy = new RunnableProxyGenerator().generate(
    			(Runnable) test,
    			getClass().getClassLoader());  

        ArooaSession session = new StandardArooaSession();
        
        ((ArooaSessionAware) proxy).setArooaSession(session);
        
        MyStateListener l = new MyStateListener();
        ((Stateful) proxy).addStateListener(l);
        ((Runnable) proxy).run();
        assertEquals("JobState", JobState.EXCEPTION, 
                l.lastEvent.getState());
        ((Resetable) proxy).softReset();
        assertEquals("JobState", JobState.READY, 
                l.lastEvent.getState());
    }
    
    /** Listener fixture. */
    private class MyStateListener implements StateListener {
        StateEvent lastEvent;
        public void jobStateChange(StateEvent event) {
            lastEvent = event;
        }
    }
    
    
    public void testStop() throws InterruptedException, FailedToStopException {
    	
    	final CountDownLatch latch = new CountDownLatch(1);
    	
    	Runnable test = new Runnable() {
			@Override
			public void run() {   					
				latch.countDown();
				synchronized(this) {
					try {
						wait();
					} catch (InterruptedException e){
						Thread.currentThread().interrupt();
					}
				}
			}
			@Override
			public String toString() {
				return "StopTestJob";
			}
		};
    	
        Runnable wrapper = (Runnable) new RunnableProxyGenerator().generate(
    			(Runnable) test,
    			getClass().getClassLoader());  
    	    	
    	Stateful stateful = (Stateful) wrapper;
    	
    	StateSteps states = new StateSteps(stateful);
    	
    	states.startCheck(JobState.READY, JobState.EXECUTING);
    	
    	Thread t = new Thread(wrapper);
    	t.start();
    	
    	states.checkWait();
    	latch.await();
    	
    	Stoppable stoppable = (Stoppable) wrapper;
    	
    	states.startCheck(JobState.EXECUTING, JobState.COMPLETE);
    	
    	stoppable.stop();
    	
    	states.checkWait();
    }
    
    /**
     * Test default Object implementation of hash code is sufficient
     * to store a wrapper in a hashmap. 
     * 
     * This test is a bit weak.
     *
     */
    public void testHashCode() {
    	Runnable wrapped = new Runnable() {
    		public void run() {}
    	};

        Runnable test = (Runnable) new RunnableProxyGenerator().generate(
    			(Runnable) wrapped,
    			getClass().getClassLoader());  
        
    	HashMap<Object, String> hashMap = new HashMap<Object, String>();
    	hashMap.put(test, "Hello");
    	
    	String result = hashMap.get(test);
    	assertEquals("Hello", result);
    }
    
    /**
     * Test the equals method.
     *
     */
    public void testEquals() {
    	Runnable wrapped = new Runnable() {
    		public void run() {}
    	};

        Runnable test = (Runnable) new RunnableProxyGenerator().generate(
    			(Runnable) wrapped,
    			getClass().getClassLoader());  
            	
    	assertTrue(test.equals(test));
    	assertEquals(test, test);
    }

    /** Bean fixture. */
    public static class Bean {
    	String greeting;
    	public void setGreeting(String greeting) {
    		this.greeting = greeting;
    	}
    	public String getGreeting() {
    		return greeting;
    	}
    }
    
    /** Job with result fixture. */
    public static class Job implements Runnable {
    	public String result;
    	public void run() {	
    	}
    	public void setResult(String result) {
    		this.result = result;
    	}
    	public String getResult() {
    		return result;
    	}
    }    
    
    /**
     * A simple test in oddjob. Ensures the wrapped job runs and the 
     * state is OK.
     */
    public void testInOddjob() throws Exception {
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <bean class='" + OurRunnable.class.getName() + "' id='r' />" +
    		" </job>" +
    		"</oddjob>";
    	
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	oddjob.run();
    	
    	Object r = new OddjobLookup(oddjob).lookup("r");
    	assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(r));
    	
    	Object ran = PropertyUtils.getProperty(r, "ran");
    	assertEquals(Boolean.class, ran.getClass());
    	assertEquals(new Boolean(true), ran);
    	
    	Map<String, String> description = ((Describeable) r).describe();
    	assertEquals("true", description.get("ran"));
    	
    	oddjob.destroy();
    }

    /** 
     * A mock class with all the properties. Implements
     * Runnable so that a proxy wrapper will be created.
     */
    public static class LotsOfProperties implements Runnable {
    	private Map<String, Object> map = new HashMap<String, Object>();
    	private String[] indexed = new String[1];
    	private String simple;
    	
    	public void run() {}
    	
    	public void setMapped(String name, Object value) {
    		map.put(name, value);
    	}
    	public Object getMapped(String name) {
    		return map.get(name);
    	}
    	public void setIndexed(int i, String value) {
    		this.indexed[i] = value;
    	}
    	public String[] getIndexed() {
    		return this.indexed;
    	}
    	public void setSimple(String simple) {
    		this.simple = simple;
    	}
    	public String getSimple() {
    		return this.simple;
    	}
    }
    
    /**
     * Test that all property setting and getting works
     * for a wrapped job by testing the proxy created.
     */
    public void testPropertiesInProxy() throws Exception {
    	LotsOfProperties bean = new LotsOfProperties();
    	
        Runnable test = (Runnable) new RunnableProxyGenerator().generate(
    			(Runnable) bean,
    			getClass().getClassLoader());  
    	
    	DynaBean db = (DynaBean) test;
    	
    	db.set("simple", "test");
    	assertEquals("test", db.get("simple"));
    	
    	db.set("mapped", "simple", "test");
    	assertEquals("test", db.get("mapped", "simple"));
    	
    	db.set("indexed", 0, "test");
    	assertEquals("test", db.get("indexed", 0));
    	
    	// check via property utils which exercises the underlying DynaClass
    	PropertyUtils.setProperty(db, "simple", "test");
    	assertEquals("test", PropertyUtils.getProperty(db, "simple"));
    	
    	PropertyUtils.setProperty(db, "mapped(simple)", "test");
    	assertEquals("test", PropertyUtils.getProperty(db, "mapped(simple)"));
    	
    	PropertyUtils.setProperty(db, "indexed[0]", "test");
    	assertEquals("test", PropertyUtils.getProperty(db, "indexed[0]"));
    }
    
    /**
     * Test that all property setting and getting works
     * for a wrapped job when in Oddjob.
     */
    public void testProperitesInOddjob() throws Exception{
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <sequential>" +
    		"   <jobs>" +
    		"    <set>" +
    		"     <values>" +
    		"      <value key='test.simple' value='test'/>" +
    		"      <value key='test.mapped(akey)' value='test'/>" +
    		"      <value key='test.indexed[0]' value='test'/>" +
    		"     </values>" +
    		"    </set>" +
    		"    <bean class='" + LotsOfProperties.class.getName() + "' id='test' />" + 
    		"    <variables id='v'>" +
    		"     <simple>" +
    		"      <value value='${test.simple}'/>" +
    		"     </simple>" +
    		"     <mapped>" +
    		"      <properties>" +
    		"       <values>" +
    		"        <value key='akey' value='${test.mapped(akey)}'/>" +
    		"       </values>" +
    		"      </properties>" +
    		"     </mapped>" +
    		"     <indexed>" +
    		"      <list>" +
    		"       <values>" +
    		"        <value value='${test.indexed[0]}'/>" +
    		"       </values>" +
    		"      </list>" +
    		"     </indexed>" +
    		"    </variables>" +
    		"   </jobs>" +
    		"  </sequential>" +
    		" </job>" +
    		"</oddjob>";
    	
    	Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
    	
    	oj.run();
    	
    	OddjobLookup lookup = new OddjobLookup(oj);
    	
    	assertEquals("test", lookup.lookup("v.simple", String.class));

    	Map<?, ?> map = lookup.lookup("v.mapped", Properties.class);
    	assertEquals(map.get("akey"), "test");

    	Object[] array = lookup.lookup("v.indexed", Object[].class);
    	assertEquals(array[0], "test");
    	
    	oj.destroy();
    }
    
    /** A Logger fixture. */
    public static class AnyLogger implements Runnable { 
    	public void run() {
    		Logger.getLogger("AnyLogger").error("FINDME");
    	}
    }
    
    /**
     * Test logging.
     * 
     * @throws Exception
     */
    public void testDefaultLogger() throws Exception {
    	class MyL implements LogListener {
    		StringBuffer messages = new StringBuffer();
    		public void logEvent(LogEvent logEvent) {
    			messages.append(logEvent.getMessage());
    		}
    	}
    	
    	AnyLogger l = new AnyLogger();
    	
        Runnable proxy = (Runnable) new RunnableProxyGenerator().generate(
    			(Runnable) l,
    			getClass().getClassLoader());  
    	
    	Logger.getLogger("AnyLogger").setLevel(Level.DEBUG);
    	
    	String proxyLoggerName = ((LogEnabled) proxy).loggerName();
    
    	assertEquals(AnyLogger.class.getName(),
    			proxyLoggerName.substring(0, AnyLogger.class.getName().length()));
    	
    	Logger.getLogger(proxyLoggerName).setLevel(Level.DEBUG);
    	
    	Log4jArchiver archiver = new Log4jArchiver(proxy, "%m%n");
    	
    	MyL ll = new MyL();
    	archiver.addLogListener(ll, proxy, LogLevel.DEBUG, 0, 1000);
    	
    	proxy.run();
    	
    	assertTrue(ll.messages.indexOf("FINDME") > 0);
    }
    
    public static class MyLogger implements Runnable, LogEnabled {
    	public String loggerName() {
    		return "MyLogger";
    	}
    	public void run() {
    		Logger.getLogger(loggerName()).error("FINDME");
    	}
    }
    
    public void testSpecificLogger() throws Exception {

    	class MyL implements LogListener {
    		StringBuffer messages = new StringBuffer();
    		public void logEvent(LogEvent logEvent) {
    			messages.append(logEvent.getMessage());
    		}
    	}
    	
    	MyLogger l = new MyLogger();
    	
        Runnable proxy = (Runnable) new RunnableProxyGenerator().generate(
    			(Runnable) l,
    			getClass().getClassLoader());  
        
    	assertEquals("MyLogger", ((LogEnabled) proxy).loggerName());
    	Logger.getLogger(((LogEnabled) proxy).loggerName()).setLevel(Level.DEBUG);
    	
    	Log4jArchiver archiver = new Log4jArchiver(proxy, "%m%n");
    	
    	MyL ll = new MyL();
    	archiver.addLogListener(ll, proxy, LogLevel.DEBUG, 0, 1000);
    	
    	proxy.run();
    	
    	assertTrue(ll.messages.indexOf("FINDME") > 0);
    }
 
    /**
     * Test describing a component via a RunnableWrapper.
     *
     */
    public void testDescribe() {
    	
    	ArooaSession session = new StandardArooaSession();
    	
    	Job j = new Job();
    	j.setResult("Hello");
    	
        Runnable wrapper = (Runnable) new RunnableProxyGenerator().generate(
    			(Runnable) j,
    			getClass().getClassLoader());  

        ((ArooaSessionAware) wrapper).setArooaSession(session);
        
    	Map<String, String> m = new UniversalDescriber(
				session).describe(wrapper);
    	
    	assertEquals("Hello", m.get("result"));
    }
    
    public static class Stubbon implements ArooaLifeAware {

    	boolean ouch;
    	
    	public void configured() {
    		// TODO Auto-generated method stub
    		
    	}
    	public void destroy() {
    		
    		if (!ouch) {
    			ouch = true;
    			throw new RuntimeException("Ouch!");
    		}
    	}
    	public void initialised() {
    		// TODO Auto-generated method stub
    		
    	}
        public String toString() {
        	return "Stubbon";
        }
    }
    
    /**
     * Test when destroy fails.
     *
     * @throws Exception
     */
    public void testDestroyInOddjob() throws Exception {
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <folder>" +
    		"   <jobs>" +
    		"    <bean class='" + OurRunnable.class.getName() + "'/>" +
    		"    <bean class='" + Stubbon.class.getName() + "'/>" +
    		"   </jobs>" +
    		"  </folder>" +
    		" </job>" +
    		"</oddjob>";
    	
    	Oddjob oj = new Oddjob();
    	oj.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	oj.run();
    	
    	assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(oj));
    	
    	try {
    		oj.destroy();
    		fail("Should fail!");
    	} catch (RuntimeException e) {
    		assertEquals("Ouch!", e.getMessage());
    	}
    	
    	oj.destroy();
    }    
}

