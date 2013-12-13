package org.oddjob.jobs.structural;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.OddjobTestHelper;

public class SequentialJobInOddjobTest extends TestCase {

	public static class OurService {
		
		public void start() {
			
		}
		
		public void stop() {
			
		}
	}
	
	public void testException() throws FailedToStopException {
		
		String xml = 
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <sequential id='test'>" +
			"   <jobs>" +
			"    <bean id='1' class='" + OurService.class.getName() + "'/>" +
			"    <state:flag id='2' state='EXCEPTION'/>" + 
			"    <state:flag id='3' state='COMPLETE'/>" +
			"    <folder/>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals(ServiceState.STARTED, 
				OddjobTestHelper.getJobState(lookup.lookup("1")));
		assertEquals(JobState.EXCEPTION, 
				OddjobTestHelper.getJobState(lookup.lookup("2")));
		assertEquals(JobState.READY, 
				OddjobTestHelper.getJobState(lookup.lookup("3")));
		
		assertEquals(ParentState.STARTED, 
				OddjobTestHelper.getJobState(lookup.lookup("test")));

		oddjob.stop();
		
		assertEquals(ParentState.EXCEPTION, 
				OddjobTestHelper.getJobState(lookup.lookup("test")));
		
		oddjob.destroy();
	}
	
}
