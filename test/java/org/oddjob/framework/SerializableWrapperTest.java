/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import org.oddjob.OjTestCase;

import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.MockComponentPersister;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.persist.OddjobPersister;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;

public class SerializableWrapperTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(SerializableWrapperTest.class);
	
	public static class Test1 implements Runnable, Serializable {
		private static final long serialVersionUID = 20051231;
		private String check;
		
		public void run() {
			if (check != null) {
				check = "good bye";
			}
			else {
				check = "hello";
			}
		}

		public String getCheck() { 
			return check; 
		}
		
		@Override
		public String toString() {
			return "Test1";
		}
	}
	
	public static class Test2 implements Runnable {
		public void run() {}
		
		@Override
		public String toString() {
			return "Test2";
		}
	}
	
   @Test
	public void testSimple() throws Exception {	

		Runnable test = new Test1();
		
        Runnable proxy = (Runnable) new RunnableProxyGenerator().generate(
    			(Runnable) test,
    			getClass().getClassLoader());  
	
		proxy.run();
		
		DynaBean copy = (DynaBean) OddjobTestHelper.copy(proxy);
		
		assertTrue(copy instanceof Proxy);
		
		assertEquals("hello", copy.get("check"));
	}
	
   @Test
	public void testNotSerializable () throws Exception {
		Runnable test = new Test2();
		
        Runnable proxy = (Runnable) new RunnableProxyGenerator().generate(
    			(Runnable) test,
    			getClass().getClassLoader());  
		
		assertTrue(proxy instanceof Transient);
	}
	
	private class OurPersister implements OddjobPersister {

		Object save;
		int count;
		public ComponentPersister persisterFor(String id) {
			return new MockComponentPersister() {
				boolean closed;
				@Override
				public void persist(String id, Object proxy, ArooaSession session) {
					if (closed) {
						return;
					}
					logger.info("Persisting [" + proxy + "] id [" + id + "]");
					assertEquals("test", id);

					try {
						save = OddjobTestHelper.copy(proxy);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					
					++count;
				}
				
				@Override
				public Object restore(String id, ClassLoader classLoader, 
						ArooaSession session) {
					assertEquals("test", id);
					
					logger.info("Restoring id [" + id + "]");
					
					return save;
				}
				
				@Override
				public void remove(String id, ArooaSession session) {
					if (closed) {
						return;
					}
					save = null;
				}

				@Override
				public void close() {
					closed = true;
				}
			};
		}		
	}
	
   @Test
	public void testSerializeInOddjob() {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean class='" + Test1.class.getName() + "' id='test'/>" +
			" </job>" +
			"</oddjob>";

		
		Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		OurPersister persister = new OurPersister();
		oddjob.setPersister(persister);
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		Proxy proxy = (Proxy) new OddjobLookup(oddjob).lookup("test");
		Test1 test1 = (Test1) ((WrapperInvocationHandler) Proxy.getInvocationHandler(
				proxy)).getWrappedComponent();

		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(proxy));		
		assertEquals("hello", test1.check);
		
		oddjob.destroy();

		// restore
		
		Oddjob oddjob2 = new Oddjob();
    	oddjob2.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob2.setPersister(persister);
		
		oddjob2.load();
		
		assertEquals(ParentState.READY, oddjob2.lastStateEvent().getState());
		
		proxy = (Proxy) new OddjobLookup(oddjob2).lookup("test");
		test1 = (Test1) ((WrapperInvocationHandler) Proxy.getInvocationHandler(
				proxy)).getWrappedComponent();
		
		assertEquals(JobState.COMPLETE, 
				((Stateful) proxy).lastStateEvent().getState());
		assertEquals("hello", test1.check);
		
		assertEquals(1, persister.count);
		
		// try and reset

		((Resetable) proxy).hardReset();
		
		assertEquals(JobState.READY, 
				((Stateful) proxy).lastStateEvent().getState());
		
		((Runnable) proxy).run();
		
		assertEquals(JobState.COMPLETE, 
				((Stateful) proxy).lastStateEvent().getState());
		
		oddjob2.destroy();
	}
	
   @Test
	public void testPersistCount() {
		
		Oddjob oddjob = new Oddjob();
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <bean class='" + Test1.class.getName() + "' id='test'/>" +
				" </job>" +
				"</oddjob>";
		
    	oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		OurPersister persister = new OurPersister();
		
		oddjob.setPersister(persister);
		
		oddjob.run();

		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		Object proxy = new OddjobLookup(oddjob).lookup("test");
		
		((Resetable) proxy).hardReset();
		
		((Runnable) proxy).run();
		
		assertEquals(3, persister.count);
		
		oddjob.destroy();
	}
}
