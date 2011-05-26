package org.oddjob.state;


import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;

import junit.framework.TestCase;

public class EqualsStateTest extends TestCase {

	public void testComplete() {
		
		EqualsState test = new EqualsState();
		
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		test.setJob(job);
		
		test.run();
		
		assertEquals(JobState.INCOMPLETE, test.lastJobStateEvent().getJobState());
		
		job.setState(JobState.COMPLETE);
		
		test.softReset();
		
		assertEquals(JobState.READY, job.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
	}
	
	public void testNotComplete() {
		
		EqualsState test = new EqualsState();
		test.setNot(true);
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		test.setJob(job);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		job.setState(JobState.COMPLETE);
		
		job.softReset();
		
		job.run();
		
		assertEquals(JobState.INCOMPLETE, test.lastJobStateEvent().getJobState());
	}

	public void testNotException() {
		
		EqualsState test = new EqualsState();
		test.setNot(true);
		test.setState(JobState.EXCEPTION);
		
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		test.setJob(job);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		job.setState(JobState.EXCEPTION);
		
		job.softReset();
		
		job.run();
		
		assertEquals(JobState.INCOMPLETE, test.lastJobStateEvent().getJobState());
	}
	
	public void testInOddjob() {
		
		String xml = 
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:equals state='INCOMPLETE'>" +
			"   <job>" +
			"    <state:flag state='INCOMPLETE'/>" +
			"   </job>" +
			"  </state:equals>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));

		oddjob.run();
		
		assertEquals(JobState.COMPLETE, oddjob.lastJobStateEvent().getJobState());
	}
	
}
