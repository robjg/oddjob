package org.oddjob.state;


import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;

public class EqualsStateTest extends TestCase {

	public void testComplete() {
		
		EqualsState test = new EqualsState();
		
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		test.setJob(job);
		
		test.run();
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
		
		job.setState(JobState.COMPLETE);
		
		test.softReset();
		
		assertEquals(JobState.READY, job.lastStateEvent().getState());
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
	}
	
	public void testNotComplete() {
		
		EqualsState test = new EqualsState();
		test.setState(new IsNot(StateConditions.COMPLETE));
		
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		test.setJob(job);
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		job.setState(JobState.COMPLETE);
		
		job.softReset();
		
		job.run();
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
	}

	public void testNotException() {
		
		EqualsState test = new EqualsState();
		test.setState(new IsNot(StateConditions.EXCEPTION));
		
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		test.setJob(job);
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		job.setState(JobState.EXCEPTION);
		
		job.softReset();
		
		job.run();
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
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
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
	}
	
}
