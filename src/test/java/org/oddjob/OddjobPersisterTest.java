package org.oddjob;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.MockComponentPersister;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.Path;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.persist.MapPersister;
import org.oddjob.persist.OddjobPersister;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateEvent;
import org.oddjob.tools.OddjobTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OddjobPersisterTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(OddjobPersisterTest.class);
	
	
    @Before
    public void setUp() throws Exception {
		logger.info("----------------- " + getName() + " -------------");
	}
	
    private class OurPersister implements OddjobPersister {

    	private final Path rootPath;

    	private Map<String, InnerPersister> persisters = 
    		new HashMap<String, InnerPersister>();
    	
    	private List<String> persisterIds = new ArrayList<String>();
    	
    	public OurPersister(String rootPath) {
    		this.rootPath = new Path(rootPath);
		}
    	
    	public ComponentPersister persisterFor(String persisterId) {
    		persisterIds.add(persisterId);
    		String path = persisterId;
    		if (path  == null) {
    			path = rootPath.toString();
    		}
    		InnerPersister inner = persisters.get(path); 
    		if (inner == null) {
    			inner = new InnerPersister(path);
        		persisters.put(path, inner);
        		logger.debug("Adding Persister " + path);
    		}
    		else {
        		logger.debug("Using Persister " + path);
    		}
    		return inner;
    	}
    	
    	private class InnerPersister extends MockComponentPersister
    	implements OddjobPersister {
 
    		private final String path;
    		
        	private boolean closed;
        	
        	Map<String, Object> store = new HashMap<String, Object>();
        	
    		public InnerPersister(String path) {
    			this.path = path;
			}
    		
    		@Override
    		public ComponentPersister persisterFor(String id) {
    			return OurPersister.this.persisterFor(
    					new Path(this.path).addId(id).toString());
    		}
    		
    		@Override
    		public void close() {
    			closed = true;
    		}
    		
    		@Override
    		public void persist(String id, Object proxy, ArooaSession session) {
    			if (closed) {
    				return;
    			}
    			try {
    				store.put(id, OddjobTestHelper.copy(proxy));
    			} catch (Exception e) {
    				throw new RuntimeException(e);
    			}

    		}

    		@Override
    		public Object restore(String id, ClassLoader classLoader, ArooaSession session) {

    			return store.remove(id);
    		}
    	    	
    		@Override
    		public void remove(String id, ArooaSession session) {
    			if (closed) {
    				return;
    			}
    			store.remove(id);
    			logger.info("removed " + id);
	    	}	    	
    	}    	
    	
    }
    
   @Test
    public void testPersist() throws PropertyVetoException {
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <echo id='e'>Hello</echo>" +
    		" </job>" +
    		"</oddjob>";
    		
    	Oddjob test = new Oddjob();
    	
    	OurPersister persister = new OurPersister("root");
    	
    	test.setPersister(persister);
		test.setConfiguration(new XMLConfiguration("XML", xml));
    	
//    	OddjobExplorer explorer = new OddjobExplorer();
//    	explorer.setOddjob(test);
//    	explorer.run();
    			
		assertEquals(0, persister.persisters.size());
		
    	test.run();

		OurPersister.InnerPersister inner = persister.persisters.get("root");
		
		assertEquals(1, inner.store.size());
		assertTrue(inner.store.containsKey("e"));
    	
    	test.hardReset();
    	    	
    	test.run();
    	
    	assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(
    			new OddjobLookup(test).lookup("e")));	
    	
    	test.destroy();
    }
    
	String xml = 
		"<oddjob xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
		" <job>" +
		"  <oddjob id='nested'>" +
		"   <configuration>" +
		"      <xml>" +
		"       <oddjob>" +
		"        <job>" +
		"         <oddjob id='inner'>" +
		"          <configuration>" +
		"             <xml>" +
		"              <oddjob>" +
		"               <job>" +
		"                <echo id='e'>Hello</echo>" +
		"               </job>" +
		"              </oddjob>" +
		"             </xml>" +
		"          </configuration>" +
		"         </oddjob>" +
		"        </job>" +
		"       </oddjob>" +
		"      </xml>" +
		"   </configuration>" +
		"  </oddjob>" +
		" </job>" +
		"</oddjob>";
	
   @Test
	public void testNestedPersisterResets() throws PropertyVetoException, ArooaPropertyException, ArooaConversionException {
				
		Oddjob test = new Oddjob();
		
		test.setConfiguration(new XMLConfiguration("XML", xml));
		
    	OurPersister persister = new OurPersister("root");
		
    	test.setPersister(persister);
    	
    	logger.debug("* Running *");
    	
		test.run();
	
		OurPersister.InnerPersister outer = 
			persister.persisters.get("root");
		OurPersister.InnerPersister middle = 
			persister.persisters.get("root/nested");
		OurPersister.InnerPersister inner = 
			persister.persisters.get("root/nested/inner");
		assertNotNull(inner);
		
		assertEquals(ParentState.COMPLETE, 
				test.lastStateEvent().getState());
		
    	assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(
    			new OddjobLookup(test).lookup("nested/inner/e")));	

    	assertEquals(1, outer.store.size());
    	assertEquals(1, middle.store.size());
    	assertEquals(1, inner.store.size());
    	
    	assertTrue(outer.store.containsKey("nested"));
    	assertTrue(middle.store.containsKey("inner"));
    	assertTrue(inner.store.containsKey("e"));
    	
    	assertEquals(3, persister.persisterIds.size());
    	assertEquals(null, persister.persisterIds.get(0));
    	assertEquals("root/nested", persister.persisterIds.get(1));
    	assertEquals("root/nested/inner", persister.persisterIds.get(2));
    	
    	logger.debug("* Resetting *");
    	
    	test.hardReset();
    	
		    	
    	logger.debug("* Running *");
    	
    	test.run();
    	    	
		
//    	OddjobExplorer explorer = new OddjobExplorer();
//    	explorer.setOddjob(test);
//    	explorer.run();
    	
    	assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(
    			new OddjobLookup(test).lookup("nested/inner/e")));	
    	
    	assertEquals(ParentState.COMPLETE, 
    			test.lastStateEvent().getState());
    	
    	logger.debug("* Resetting Middle *");
    	
    	Resettable middleOj = new OddjobLookup(test).lookup(
    			"nested", Resettable.class);

    	middleOj.hardReset();
    	
    	logger.debug("* Running Middle *");
    	
    	((Runnable) middleOj).run();
    	
    	logger.debug("* Destroying *");
    	
    	assertEquals(ParentState.COMPLETE, 
    			test.lastStateEvent().getState());
    	
    	test.destroy();
    	
    	assertEquals(ParentState.DESTROYED, 
    			test.lastStateEvent().getState());
    	
	}
	
   @Test
	public void testNestedPersisterRestore() throws PropertyVetoException, ArooaPropertyException, ArooaConversionException {
		
		Oddjob test = new Oddjob();
		
		test.setConfiguration(new XMLConfiguration("XML", xml));
		
    	OurPersister persister = new OurPersister("root");
		
    	test.setPersister(persister);
    	
    	logger.debug("* Running *");
    	
		test.run();
	
		assertEquals(ParentState.COMPLETE, 
				test.lastStateEvent().getState());
		
    	assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(
    			new OddjobLookup(test).lookup("nested/inner/e")));	

    
    	logger.debug("* Destroying *");
    	
    	test.destroy();
    	
		Oddjob copy = new Oddjob();
		
		copy.setConfiguration(new XMLConfiguration("XML", xml));
		
    	copy.setPersister(persister);
    			    	
    	logger.debug("* Restoring *");
    	
    	copy.load();    
    			
//    	OddjobExplorer explorer = new OddjobExplorer();
//    	explorer.setOddjob(test);
//    	explorer.run();
    	
    	assertEquals(ParentState.READY,
    			copy.lastStateEvent().getState());
    	
    	OddjobLookup copyLookup = new OddjobLookup(copy);
    	
    	Object middleOj = copyLookup.lookup("nested");
    	
    	assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(
    			middleOj));	
    	
    	assertEquals(null, copyLookup.lookup("nested/inner"));	
    	
    	logger.debug("* Loading Middle *");
    	
    	((Oddjob) middleOj).load();
    	
    	Object innerOj = copyLookup.lookup("nested/inner");
    	
    	assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(
    			innerOj));	
    	
    	logger.debug("* Loading Inner *");
    	
    	((Oddjob) innerOj).load();
    	
    	Object echo = copyLookup.lookup("nested/inner/e");
    	
    	assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(
    			echo));	
    	
    	logger.debug("* Destroying *");
    	
    	copy.destroy();
    	
    	assertEquals(ParentState.DESTROYED, 
    			copy.lastStateEvent().getState());
    	
	}

   @Test
	public void testResetsBeforeLoad() throws PropertyVetoException, ArooaPropertyException, ArooaConversionException {
		
		Oddjob test = new Oddjob();
		
		test.setConfiguration(new XMLConfiguration("XML", xml));
		
    	OurPersister persister = new OurPersister("root");
		
    	test.setPersister(persister);
    	
    	logger.debug("* Running *");
    	
		test.run();
	
		StateEvent jse1 = test.lastStateEvent();
		assertEquals(ParentState.COMPLETE, jse1.getState());
		
    	assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(
    			new OddjobLookup(test).lookup("nested/inner/e")));	

    
    	logger.debug("* Destroying *");
    	
    	test.destroy();
    	
		Oddjob copy = new Oddjob();
		
		copy.setConfiguration(new XMLConfiguration("XML", xml));
		
    	copy.setPersister(persister);
    			    	
    	logger.debug("* Restoring *");
    	
    	copy.hardReset();
    	    	
    	copy.run();    
    			
//    	OddjobExplorer explorer = new OddjobExplorer();
//    	explorer.setOddjob(test);
//    	explorer.run();

		
    	assertEquals(ParentState.COMPLETE,
    			copy.lastStateEvent().getState());
    	
    	assertTrue(copy.lastStateEvent().getTime().getTime() 
    			> jse1.getTime().getTime());
    	
    	OddjobLookup copyLookup = new OddjobLookup(copy);
    	
    	Object middleOj = copyLookup.lookup("nested");
    	
    	assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(
    			middleOj));	
    	
    	Object innerOj = copyLookup.lookup("nested/inner");
    	
    	assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(
    			innerOj));	
    	
    	Object echo = copyLookup.lookup("nested/inner/e");
    	
    	assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(
    			echo));	
    	
    	logger.debug("* Destroying *");
    	
    	assertEquals(ParentState.COMPLETE, 
    			copy.lastStateEvent().getState());
    	
    	copy.destroy();
    	
    	assertEquals(ParentState.DESTROYED, 
    			copy.lastStateEvent().getState());
    	
	}
	
	public static class FailOnce implements Runnable, Serializable {
		private static final long serialVersionUID = 2010031700L;
		
		boolean ok;
		
		@Override
		public void run() {
			if (!ok) {
				ok = true;
				throw new RuntimeException("I'm not OK yet!!!!");
			}
		}		
	}
	
	
	String failureXml = 
		"<oddjob xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
		" <job>" +
		"  <oddjob id='nested'>" +
		"   <configuration>" +
		"      <xml>" +
		"       <oddjob>" +
		"        <job>" +
		"         <oddjob id='inner'>" +
		"          <configuration>" +
		"             <xml>" +
		"              <oddjob>" +
		"               <job>" +
		"                <bean id='e' class='" + FailOnce.class.getName() + "'/>" +
		"               </job>" +
		"              </oddjob>" +
		"             </xml>" +
		"          </configuration>" +
		"         </oddjob>" +
		"        </job>" +
		"       </oddjob>" +
		"      </xml>" +
		"   </configuration>" +
		"  </oddjob>" +
		" </job>" +
		"</oddjob>";
	
	
   @Test
	public void testSoftResetsBeforeLoad() {
		
		Oddjob test = new Oddjob();
		
		test.setConfiguration(new XMLConfiguration("XML", failureXml));
		
    	OurPersister persister = new OurPersister("root");
		
    	test.setPersister(persister);
    	
    	logger.debug("* Running *");
    	
		test.run();
	
		assertEquals(ParentState.EXCEPTION, 
				test.lastStateEvent().getState());
		
    	assertEquals(JobState.EXCEPTION, OddjobTestHelper.getJobState(
    			new OddjobLookup(test).lookup("nested/inner/e")));	

    
    	logger.debug("* Destroying *");
    	
    	test.destroy();
    	
		Oddjob copy = new Oddjob();
		
		copy.setConfiguration(new XMLConfiguration("XML", failureXml));
		
    	copy.setPersister(persister);
    			    	
    	logger.debug("* Restoring *");
    	
    	copy.softReset();
    	    	
    	copy.run();    
    			
		assertEquals(ParentState.COMPLETE, 
				copy.lastStateEvent().getState());
		
    	assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(
    			new OddjobLookup(copy).lookup("nested/inner/e")));	

    
    	logger.debug("* Destroying *");
    	
    	assertEquals(ParentState.COMPLETE, 
    			copy.lastStateEvent().getState());    	
    	
    	copy.destroy();
    	
    	assertEquals(ParentState.DESTROYED, 
    			copy.lastStateEvent().getState());    	
	}   	
	
	/**
	 * 
	 */
   @Test
	public void testResetBeforeLoadPersister() throws ComponentPersistException {
		
		MapPersister persister = new MapPersister();
		
		final ComponentPersister compPersister = persister.persisterFor(null);
		
		ArooaSession session = new StandardArooaSession() {
			public ComponentPersister getComponentPersister() {
				return compPersister;
			}
		};
		
		Oddjob oddjob = new Oddjob();
		
		OddjobTestHelper.register(oddjob, session, "test");
		
		oddjob.setArooaSession(session);
		
		oddjob.hardReset();
		
		Object copy = persister.persisterFor(null).restore("test", 
				getClass().getClassLoader(), session);
		
		assertNotNull(copy);
	}
}
