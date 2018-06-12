/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.ArooaException;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.types.XMLConfigurationType;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateListener;
import org.oddjob.state.State;
import org.oddjob.state.StateEvent;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.OurDirs;
import org.oddjob.util.URLClassLoaderType;

/**
 * Mainly the same tests as OddjobTest but only for load(), not run().
 * 
 * @author Rob Gordon.
 */
public class OddjobLoadTest extends OjTestCase {
	
	/**
	 * Test resetting Oddjob
	 *
	 */
   @Test
	public void testReset() {
		class MyL implements StructuralListener {
			int count;
			public void childAdded(StructuralEvent event) {
				count++;
			}
			public void childRemoved(StructuralEvent event) {
				count--;
			}
		}

		class MySL implements StateListener {
			List<State> states = new ArrayList<State>();
			
			public void jobStateChange(StateEvent event) {
				states.add(event.getState());
			}
		}
		
		String xml = 
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:flag id='flag' state='COMPLETE'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		MyL childListener = new MyL();
		oj.addStructuralListener(childListener);
		
		assertEquals(0, childListener.count);
		
		oj.load();
		
		Stateful flag = (Stateful) new OddjobLookup(oj).lookup("flag");
		
		MySL stateListener = new MySL();
		flag.addStateListener(stateListener);
		
		assertEquals(1, stateListener.states.size());
		assertEquals(JobState.READY, stateListener.states.get(0));
		assertEquals(1, childListener.count);
		
		oj.hardReset();
		
		assertEquals(2, stateListener.states.size());
		assertEquals(JobState.DESTROYED, stateListener.states.get(1));
		assertEquals(0, childListener.count);
		
		oj.load();
		
		// new job.
		assertEquals(2, stateListener.states.size());
		
		assertEquals(1, childListener.count);		
		
		oj.hardReset();
		
		assertEquals(0, childListener.count);
		
		oj.load();
		
		assertEquals(1, childListener.count);
		
		oj.destroy();
	}
	
	/**
	 * Test reseting Oddjob
	 *
	 */
   @Test
	public void testSoftReset() {
		class MyL implements StructuralListener {
			int count;
			public void childAdded(StructuralEvent event) {
				count++;
			}
			public void childRemoved(StructuralEvent event) {
				count--;
			}
		}

		String xml =
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:flag state='INCOMPLETE'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		MyL l = new MyL();
		oj.addStructuralListener(l);
		
		assertEquals(0, l.count);
		
		oj.load();
		
		assertEquals(1, l.count);
		assertEquals(ParentState.READY, oj.lastStateEvent().getState());
		
		oj.softReset();
		
		assertEquals(1, l.count);
		assertEquals(ParentState.READY, oj.lastStateEvent().getState());
		
		oj.load();
		
		assertEquals(1, l.count);		
		assertEquals(ParentState.READY, oj.lastStateEvent().getState());
		
		oj.softReset();
		
		assertEquals(1, l.count);
		assertEquals(ParentState.READY, oj.lastStateEvent().getState());
	
		oj.load();
		
		assertEquals(1, l.count);		
		assertEquals(ParentState.READY, oj.lastStateEvent().getState());
		
		oj.destroy();
	}
	
	/**
	 * Test Oddjob can be rerun after a configuration
	 * failure using a soft reset.
	 *
	 */
   @Test
	public void testSoftResetOnFailure() {

		String xml =
			"<oddjob>" +
			" <job>" +
			"  <idontexist/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
				
		oj.run();
		
		assertEquals(ParentState.EXCEPTION, oj.lastStateEvent().getState());
		
		String xml2 =
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:flag state='COMPLETE'/>" +
			" </job>" +
			"</oddjob>";
				
		oj.setConfiguration(new XMLConfiguration("XML", xml2));
		oj.load();
		
		assertEquals(ParentState.EXCEPTION, oj.lastStateEvent().getState());

		oj.softReset();
		
		assertEquals(ParentState.READY, oj.lastStateEvent().getState());
		
		oj.run();

		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());		
		
		oj.destroy();
	}
	
	/**
	 * Test loading an Oddjob without a child.
	 * @throws ArooaParseException 
	 *
	 */
   @Test
    public void testLoadNoChild() throws ArooaParseException {
        String config = "<oddjob id='this'/>";
        
        Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", config));
        test.load();
        
        Object root = new OddjobLookup(test).lookup("this");
        
        assertEquals(Oddjob.OddjobRoot.class, root.getClass());
        
        assertEquals(ParentState.READY, test.lastStateEvent().getState());
        
        test.hardReset();
        
        test.load();
        
        assertEquals(ParentState.READY, test.lastStateEvent().getState());
        
        test.destroy();
    }

   @Test
    public void testLoadNoFile() {
    	
		OurDirs ourDirs = new OurDirs();
		
    	File file = ourDirs.relative("work/oddjob-test.xml");
    	file.delete();
    	
    	Oddjob test = new Oddjob();
    	test.setFile(file);
    	
    	test.load();
    	
    	assertEquals(ParentState.READY, OddjobTestHelper.getJobState(test));
    	
    	assertTrue(file.exists());
    	
    	test.destroy();
    }
    
    /**
     * Test a nested Oddjob.
     * @throws ArooaParseException 
     *
     */
   @Test
    public void testLoadNestedOddjob() throws ArooaParseException {
        String config = 
        	"<oddjob>" +
        	" <job>" +
        	"  <oddjob id='nested' " +
        	"          xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
        	"   <configuration>" +
        	"    <xml>" +
        	"     <oddjob/>" +
        	"    </xml>" +
        	"   </configuration>" +
        	"  </oddjob>" +
        	" </job>" +
        	"</oddjob>";

        Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));
        
        oj.load();

        assertEquals(ParentState.READY, 
        		oj.lastStateEvent().getState());
        
        Oddjob test = (Oddjob) new OddjobLookup(oj).lookup("nested");
        assertNotNull("Nested oddjob", test);
        
        assertEquals(ParentState.READY, 
        		test.lastStateEvent().getState());
        
        test.hardReset();
        
        test.load();
        
        assertEquals(ParentState.READY, 
        		test.lastStateEvent().getState());
        
        oj.destroy();
    }

   @Test
    public void testLoadOddjobClassloader() throws ArooaConversionException {
    	
    	
        String config = 
        	"<oddjob>" +
        	" <job>" +
        	"  <oddjob id='nested'>" +
            "   <classLoader>" +
            "    <bean class='" + URLClassLoaderType.class.getName() + "'>" +
            "     <files>" +
            "      <files files='build/*.jar'/>" +
            "     </files>" +
            "    </bean>" +
            "   </classLoader>" +
            "  </oddjob>" +
            " </job>" +
            "</oddjob>";
                
        Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));
        
        oj.load();
        
        String nestedConf = "<oddjob/>";
        Oddjob test = new OddjobLookup(oj).lookup("nested", Oddjob.class);
		test.setConfiguration(new XMLConfiguration("TEST", nestedConf));
        
        test.load();
        
        assertNotNull("Nested classloader", 
                test.getClassLoader());
        
        oj.destroy();
    }
    
    
    /**
     * Test a nested lookup.
     * @throws Exception
     */
   @Test
    public void testLookup() throws Exception {

    	String nested = 
    		"<oddjob>" +
    		" <job>" +
    		"  <echo id='fruits'>apple</echo>" +
    		" </job>" +
    		"</oddjob>";
        
    	String config = 
    		"<oddjob id='this'>" +
    		" <job>" +
	    	"  <oddjob id='nested'>" +
	    	"   <configuration>" +
	    	"    <value value='${inner-config}'/>" +
	    	"   </configuration>" +
	    	"  </oddjob>" +
	    	" </job>" +
			"</oddjob>";

        Oddjob oj = new Oddjob();
        
		oj.setConfiguration(new XMLConfiguration("XML", config));
		
		XMLConfigurationType configType = new XMLConfigurationType();
		configType.setXml(nested);
		
        oj.setExport("inner-config", configType);
        
        assertTrue(oj.isLoadable());
        
        oj.load();
        
        assertFalse(oj.isLoadable());
        
        Loadable loadable = (Loadable) new OddjobLookup(oj).lookup("nested"); 
        loadable.load();
        
        String fruit = new OddjobLookup(oj).lookup(
        		"nested/fruits.text", String.class); 
        
        assertEquals("apple", fruit);
        
        oj.destroy();
        
        // Wrong!
        assertTrue(oj.isLoadable());
    }

   @Test
    public void testArgs() throws Exception {
    	
    	String config = 
    		"<oddjob id='this'>" +
    		" <job>" +
    		"  <variables id='fruits'>" +
    		"	<fruit>" +
    		"    <value value='${this.args[0]}'/>" +
    		"	</fruit>" +
    		"  </variables>" +
    		" </job>" +
    		"</oddjob>";
    	
        Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));
        oj.setArgs(new String[] { "apple" });
        
        oj.load();
        
        Runnable variables = (Runnable) new OddjobLookup(oj).lookup("fruits");
        variables.run();
        
        String fruit = new OddjobLookup(oj).lookup("fruits.fruit", String.class);
        assertEquals("apple", fruit);
        
        oj.destroy();
    }
    
   @Test
    public void testSetArgs() {

    	String config = 
    		"<oddjob>" +
    		" <job>" +
    		"  <oddjob id='nested'>" +
    		"   <args>" +
    		"    <list>" +
    		"     <values>" +
    		"      <value value='apple'/>" +
    		"     </values>" +
    		"    </list>" +
    		"   </args>" +
    		"   <configuration>" +
    		"    <xml>" +
    		"     <oddjob/>" +
    		"    </xml>" +
    		"   </configuration>" +
    		"  </oddjob>" +
    		" </job>" +
    		"</oddjob>";
        
        Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));

        oj.load();

        Loadable loadable = (Loadable) new OddjobLookup(oj).lookup("nested");

        assertTrue(loadable.isLoadable());

        loadable.load();
        
        assertFalse(loadable.isLoadable());
        
        assertEquals("apple", new OddjobLookup(oj).lookup("nested.args[0]"));
        
        oj.destroy();
    }
    
   @Test
    public void testExport() throws Exception {
    	
    	String config = 
    		"<oddjob>" +
    		" <job>" +
    		"  <oddjob id='nested'>" +
    		"   <export>" +
    		"      <value key='fruit' value='apple'/>" +
    		"   </export>" +
    		"   <configuration>" +
    		"      <xml>" +
    		"<oddjob>" +
    		" <job>" +
    		"  <variables id='fruits'>" +
    		"	<fruit>" +
    		"    <value value='${fruit}'/>" +
    		"	</fruit>" +
    		"  </variables>" +
    		" </job>" +
    		"</oddjob>" + 
    		"      </xml>" +
    		"   </configuration>" +
    		"  </oddjob>" +
    		" </job>" +
    		"</oddjob>";
        
    	    	
        Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));
        oj.load();
        
        Loadable loadable = (Loadable) new OddjobLookup(oj).lookup("nested");
        loadable.load();        
        
        Runnable runnable = (Runnable) new OddjobLookup(oj).lookup("nested/fruits");
        runnable.run();        
        
        String fruit = new OddjobLookup(oj).lookup(
        		"nested/fruits.fruit", String.class);
        assertEquals("apple", fruit);
        
        oj.destroy();
    }    
            
    /**
     * Test Component Registry management. Test Oddjob 
     * adds and clears up it's children OK.
     * @throws ArooaParseException 
     * 
     */
   @Test
    public void testRegistryManagement() throws ArooaParseException {
    	
    	final ArooaSession session = new OddjobSessionFactory().createSession();
    	
    	class OurContext extends MockArooaContext {
    		@Override
    		public ArooaSession getSession() {
    			return session;
    		}
    		
    		@Override
    		public RuntimeConfiguration getRuntime() {
    			return new MockRuntimeConfiguration() {
    				@Override
    				public void configure() throws ArooaException {
    				}
    			};
    		}
    	}
    	
    	Oddjob oj = new Oddjob();
    	oj.setArooaSession(session);
    	
    	ComponentPool pool = session.getComponentPool();
    	
    	pool.registerComponent(
    			new ComponentTrinity(
    				oj, oj, new OurContext())
    			, "oj");
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <echo id='echo'>Hello</echo>" +
    		" </job>" +
    		"</oddjob>";
    	
		oj.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	BeanDirectory dir = session.getBeanRegistry();
    	
    	assertEquals(null, dir.lookup("oj/echo"));

    	oj.load();
    	
    	assertNotNull(dir.lookup("oj/echo"));
    	
    	oj.hardReset();
    	
    	assertEquals(null, dir.lookup("oj/echo"));
    	
    	oj.load();
    	
    	assertNotNull(dir.lookup("oj/echo"));
    	
    	oj.destroy();
    }
    
    public static class ReluctantToDie extends SimpleJob {
    	boolean die;
    	@Override
    	protected int execute() throws Throwable {
    		return 0;
    	}
    	@Override
    	public void onDestroy() {
    		super.onDestroy();
    		if (!die) {
    			die = true;
    			throw new IllegalStateException("I'm not ready to die.");
    		}
    	}
    }
    
   @Test
    public void testFailedDestroy() {
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <bean class='" + ReluctantToDie.class.getName() + "'/>" +
    		" </job>" +
    		"</oddjob>";
    	
    	Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", xml));

    	test.load();
    	
    	try {
    		test.destroy();
    		fail("Exception expected.");
    	} catch (IllegalStateException e) {
    		// expected.
    	}
    	
        assertFalse(test.isLoadable());

    	assertEquals(ParentState.READY, 
    			test.lastStateEvent().getState());
    	
    	test.destroy();
    	
    	assertEquals(ParentState.DESTROYED, 
    			test.lastStateEvent().getState());
    }
}

