package org.oddjob.jobs.structural;
import org.junit.Before;

import org.junit.Test;

import java.util.Arrays;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;

public class ForEachWindowsTest extends OjTestCase {
	private static final Logger logger = Logger.getLogger(ForEachWindowsTest.class);
	
   @Before
   public void setUp() throws Exception {

		
		logger.info("--------------------  " + getName() + "  ---------------");
	}
	
   @Test
	public void testPreLoad() {
	
    	String xml =
			"<foreach id='test'>" +
			" <job>" +
    		"  <echo>${test.current}</echo>" +
    		" </job>" +
    		"</foreach>";

		ForEachJob test = new ForEachJob();
		
		ArooaSession session = new OddjobSessionFactory().createSession();
		
    	test.setArooaSession(session);
    	test.setConfiguration(new XMLConfiguration("XML", xml));
  
    	test.setValues(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    	test.setPreLoad(3);
    	
    	test.load();
    	
    	Object[] children = OddjobTestHelper.getChildren(test);
    	
    	assertEquals(3, children.length);
    	
    	test.run();
    	
    	children = OddjobTestHelper.getChildren(test);
    	
    	assertEquals(10, children.length);
    	
    	test.destroy();
	}
	
   @Test
	public void testPurgeAfter() {
		
    	String xml = 
			"<foreach id='test'>" +
			" <job>" +
    		"  <echo>${test.current}</echo>" +
    		" </job>" +
    		"</foreach>";

		ForEachJob test = new ForEachJob();
		
		ArooaSession session = new OddjobSessionFactory().createSession();
		
    	test.setArooaSession(session);
    	test.setConfiguration(new XMLConfiguration("XML", xml));
  
    	test.setValues(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    	test.setPurgeAfter(3);
    	
    	test.load();
    	
    	Object[] children = OddjobTestHelper.getChildren(test);
    	
    	assertEquals(10	, children.length);
    	
    	test.run();
    	
    	children = OddjobTestHelper.getChildren(test);
    	
    	assertEquals(3, children.length);
    	
    	test.destroy();
	}
	
   @Test
	public void testPreLoadAndPurgeAfter() {
		
    	String xml = 
			"<foreach id='test'>" +
			" <job>" +
			"  <echo>${test.current}</echo>" +
			" </job>" +
			"</foreach>";

		ForEachJob test = new ForEachJob();
		
		ArooaSession session = new OddjobSessionFactory().createSession();
		
    	test.setArooaSession(session);
    	test.setConfiguration(new XMLConfiguration("XML", xml));
  
    	test.setValues(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    	test.setPreLoad(5);
    	test.setPurgeAfter(3);
    	
    	test.load();
    	
    	Object[] children = OddjobTestHelper.getChildren(test);
    	
    	assertEquals(5, children.length);
    	
    	test.run();
    	
    	children = OddjobTestHelper.getChildren(test);
    	
    	assertEquals(3, children.length);
    	
    	test.destroy();
	}
	
   @Test
	public void testForEachWithExecutionWindowExample() {
		
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/jobs/structural/ForEachExecutionWindow.xml",
    			getClass().getClassLoader()));
    	
    	oddjob.run();
    	
    	assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
    	
    	Object[] children = OddjobTestHelper.getChildren(OddjobTestHelper.getChildren(oddjob)[0]);

    	assertEquals(3, children.length);
    	    	
    	oddjob.destroy();
    	
	}
}
