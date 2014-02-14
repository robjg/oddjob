package org.oddjob.state;

import junit.framework.TestCase;

public class IsSoftResetableTest extends TestCase {

	public void testJobStates() {
		
		StateCondition test = new IsSoftResetable();
		
		assertEquals(true, test.test(JobState.READY));
		assertEquals(false, test.test(JobState.EXECUTING));
		assertEquals(false, test.test(JobState.COMPLETE));
		assertEquals(true, test.test(JobState.INCOMPLETE));
		assertEquals(true, test.test(JobState.EXCEPTION));
	}
	
	public void testServiceStates() {
		
		StateCondition test = new IsSoftResetable();
		
		assertEquals(true, test.test(ServiceState.STARTABLE));
		assertEquals(false, test.test(ServiceState.STARTING));
		assertEquals(false, test.test(ServiceState.STARTED));
		assertEquals(true, test.test(ServiceState.EXCEPTION));
		assertEquals(false, test.test(ServiceState.STOPPED));
	}
	
	public void testParentStates() {
		
		StateCondition test = new IsSoftResetable();
		
		assertEquals(true, test.test(ParentState.READY));
		assertEquals(false, test.test(ParentState.EXECUTING));
		assertEquals(false, test.test(ParentState.STARTED));
		assertEquals(false, test.test(ParentState.ACTIVE));
		assertEquals(false, test.test(ParentState.COMPLETE));
		assertEquals(true, test.test(ParentState.INCOMPLETE));
		assertEquals(true, test.test(ParentState.EXCEPTION));
	}	
}
