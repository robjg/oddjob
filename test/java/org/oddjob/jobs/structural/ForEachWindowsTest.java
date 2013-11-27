package org.oddjob.jobs.structural;

import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.OddjobTestHelper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class ForEachWindowsTest extends TestCase {
	private static final Logger logger = Logger.getLogger(ForEachWindowsTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("--------------------  " + getName() + "  ---------------");
	}
	
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
