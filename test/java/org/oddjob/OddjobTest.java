/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.oddjob.arooa.ArooaException;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.types.XMLConfigurationType;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

/**
 *
 * @author Rob Gordon.
 */
public class OddjobTest extends TestCase {
	
	/**
	 * Test resetting Oddjob
	 *
	 */
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

		class MySL implements JobStateListener {
			List<JobState> states = new ArrayList<JobState>();
			
			public void jobStateChange(JobStateEvent event) {
				states.add(event.getJobState());
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
		
		MyL l = new MyL();
		oj.addStructuralListener(l);
		
		assertEquals(0, l.count);
		
		oj.run();
		
		Stateful flag = (Stateful) new OddjobLookup(oj).lookup("flag");
		
		MySL sl = new MySL();
		flag.addJobStateListener(sl);
		
		assertEquals(1, sl.states.size());
		assertEquals(JobState.COMPLETE, sl.states.get(0));
		assertEquals(1, l.count);
		
		oj.hardReset();
				
		assertEquals(3, sl.states.size());
		assertEquals(JobState.READY, sl.states.get(1));
		assertEquals(JobState.DESTROYED, sl.states.get(2));
		assertEquals(0, l.count);
		
		oj.run();
		
		// new job.
		assertEquals(3, sl.states.size());
		
		assertEquals(1, l.count);		
		
		oj.hardReset();
		
		assertEquals(0, l.count);
		
		oj.run();
		
		assertEquals(1, l.count);
		
		oj.destroy();
	}
	
	/**
	 * Test reseting Oddjob
	 *
	 */
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
		
		oj.run();
		
		assertEquals(1, l.count);
		assertEquals(JobState.INCOMPLETE, oj.lastJobStateEvent().getJobState());
		
		oj.softReset();
		
		assertEquals(1, l.count);
		assertEquals(JobState.READY, oj.lastJobStateEvent().getJobState());
		
		oj.run();
		
		assertEquals(1, l.count);		
		assertEquals(JobState.INCOMPLETE, oj.lastJobStateEvent().getJobState());
		
		oj.softReset();
		
		assertEquals(1, l.count);
		assertEquals(JobState.READY, oj.lastJobStateEvent().getJobState());
	
		oj.run();
		
		assertEquals(1, l.count);		
		assertEquals(JobState.INCOMPLETE, oj.lastJobStateEvent().getJobState());
		
		oj.destroy();
	}
	
	/**
	 * Test Oddjob can be rerun after a configuration
	 * failure using a soft reset.
	 *
	 */
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
		
		assertEquals(JobState.EXCEPTION, oj.lastJobStateEvent().getJobState());
		
		String xml2 =
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:flag state='COMPLETE'/>" +
			" </job>" +
			"</oddjob>";
		
		oj.setConfiguration(new XMLConfiguration("XML", xml2));
		oj.softReset();
		
		assertEquals(JobState.READY, oj.lastJobStateEvent().getJobState());
		
		oj.run();
		
		assertEquals(JobState.COMPLETE, oj.lastJobStateEvent().getJobState());		
		
		oj.destroy();
	}
	
	/**
	 * Test loading an Oddjob without a child.
	 * @throws ArooaParseException 
	 *
	 */
    public void testLoadNoChild() throws ArooaParseException {
        String config = "<oddjob id='this'/>";
        
        Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", config));
        test.run();
        Object root = new OddjobLookup(test).lookup("this");
        
        assertEquals(Oddjob.OddjobRoot.class, root.getClass());
        
        assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
        
        test.hardReset();
        
        test.run();
        
        assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
        
        test.destroy();
    }

    public void testLoadNoFile() {
    	
    	OurDirs dirs = new OurDirs();  
    	
    	File file = dirs.relative("work/oddjob-test.xml");
    	file.delete();
    	
    	Oddjob test = new Oddjob();
    	test.setFile(file);
    	
    	test.run();
    	
    	assertEquals(JobState.READY, Helper.getJobState(test));
    	
    	assertTrue(file.exists());
    	
    	test.destroy();
    }
    
    public void testLoadOddjobClassloader() {
        String config = 
        	"<oddjob>" +
        	" <job>" +
        	"  <oddjob id='nested'>" +
            "   <classLoader>" +
            "    <url-class-loader>" +
            "     <files>" +
            "      <files files='build/*.jar'/>" +
            "     </files>" +
            "    </url-class-loader>" +
            "   </classLoader>" +
            "  </oddjob>" +
            " </job>" +
            "</oddjob>";
                
        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("XML", config));

        oj.load();
        
        String nestedConf = "<oddjob/>";
        Oddjob test = (Oddjob) new OddjobLookup(oj).lookup("nested");
		oj.setConfiguration(new XMLConfiguration("TEST", nestedConf));
        test.run();
        
        assertNotNull("Nested classloader", 
                test.getClassLoader());
        
        oj.destroy();
    }
    
    
    /**
     * Test a nested lookup.
     * @throws Exception
     */
    public void testLookup() throws Exception {

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

    	String nested = 
    		"<oddjob>" +
    		" <job>" +
    		"  <variables id='fruits'>" +
    		"   <fruit>" +
    		"    <value value='apple'/>" +
    		"   </fruit>" +
    		"  </variables>" +
    		" </job>" +
    		"</oddjob>";
        
        Oddjob oj = new Oddjob();
        
		oj.setConfiguration(new XMLConfiguration("XML", config));
		
		XMLConfigurationType configType = new XMLConfigurationType();
		configType.setXml(nested);
		
        oj.setExport("inner-config", configType);
        oj.run();
        
        String fruit = new OddjobLookup(oj).lookup(
        		"nested/fruits.fruit", String.class); 
        
        assertEquals("apple", fruit);
        
        oj.destroy();
    }

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
        oj.run();
        
        String fruit = new OddjobLookup(oj).lookup("fruits.fruit", String.class);
        assertEquals("apple", fruit);
        
        oj.destroy();
    }    
    
    public void testInheritedProperties() throws Exception {
    	
    	String config = 
    		"<oddjob>" +
    		" <job>" +
    		"  <sequential>" +
    		"   <jobs>" +
    		"    <properties>" +
    		"     <values>" +
    		"      <value key='fruit' value='apple'/>" +
    		"     </values>" +
    		"    </properties>" +
    		"    <oddjob id='nested'>" +
    		"     <configuration>" +
    		"      <arooa:configuration xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
    		"       <xml>" +
    		"        <xml>" +
    		"<oddjob>" +
    		" <job>" +
    		"  <variables id='fruits'>" +
    		"	<fruit>" +
    		"    <value value='${fruit}'/>" +
    		"	</fruit>" +
    		"  </variables>" +
    		" </job>" +
    		"</oddjob>" + 
    		"        </xml>" +
    		"       </xml>" +
    		"      </arooa:configuration>" +
    		"     </configuration>" +
    		"    </oddjob>" +
    		"   </jobs>" +
    		"  </sequential>" +
    		" </job>" +
    		"</oddjob>";
        
    	    	
        Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));
        oj.run();
        
        String fruit = new OddjobLookup(oj).lookup(
        		"nested/fruits.fruit", String.class);
        assertEquals("apple", fruit);
        
        oj.destroy();
    }    
    
    /**
     * Test Oddjob dir property.
     *
     */
    public void testDir() {
    	OurDirs dirs = new OurDirs();
    	File testFile = dirs.relative("work/xyz.xml"); 
    	
    	Oddjob oj = new Oddjob();
    	oj.setFile(testFile);
    	
    	assertEquals(dirs.relative("work"), oj.getDir());
    	
    	oj.destroy();
    }
        
    /**
     * Test Component Registry management. Test Oddjob 
     * adds and clears up it's children OK.
     * @throws ArooaParseException 
     * 
     */
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
    		"  <echo id='echo' text='Hello'/>" +
    		" </job>" +
    		"</oddjob>";
    	
		oj.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	BeanDirectory dir = session.getBeanRegistry();
    	
    	assertEquals(null, dir.lookup("oj/echo"));

    	oj.run();
    	
    	assertNotNull(dir.lookup("oj/echo"));
    	
    	oj.hardReset();
    	
    	assertEquals(null, dir.lookup("oj/echo"));
    	
    	oj.run();
    	
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
    
    public void testFailedDestroy() {
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <bean class='" + ReluctantToDie.class.getName() + "'/>" +
    		" </job>" +
    		"</oddjob>";
    	
    	Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	test.run();
    	
    	try {
    		test.destroy();
    		fail("Exception expected.");
    	} catch (IllegalStateException e) {
    		// expected.
    	}
    	
    	assertEquals(JobState.COMPLETE, 
    			test.lastJobStateEvent().getJobState());
    	
    	test.destroy();
    	
    	assertEquals(JobState.DESTROYED, 
    			test.lastJobStateEvent().getJobState());
    }
}

