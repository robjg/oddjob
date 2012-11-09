package org.oddjob.state;

import junit.framework.TestCase;

public class IsDoneOrCrashedTest extends TestCase {

	public void testAllJobStates() {
		
		IsDoneOrCrashed test = new IsDoneOrCrashed();
		
		assertEquals(false, test.test(JobState.READY));
		assertEquals(false, test.test(JobState.EXECUTING));
		assertEquals(true, test.test(JobState.COMPLETE));
		assertEquals(true, test.test(JobState.INCOMPLETE));
		assertEquals(true, test.test(JobState.EXCEPTION));
		
	}
	
	public void testAllServiceStates() {
		
		IsDoneOrCrashed test = new IsDoneOrCrashed();
		
		assertEquals(false, test.test(ServiceState.READY));
		assertEquals(false, test.test(ServiceState.STARTING));
		assertEquals(true, test.test(ServiceState.STARTED));
		assertEquals(true, test.test(ServiceState.COMPLETE));
		assertEquals(true, test.test(ServiceState.EXCEPTION));
		
	}
	
	public void testAllParentStates() {
		
		IsDoneOrCrashed test = new IsDoneOrCrashed();
		
		assertEquals(false, test.test(ParentState.READY));
		assertEquals(false, test.test(ParentState.EXECUTING));
		assertEquals(false, test.test(ParentState.ACTIVE));
		assertEquals(true, test.test(ParentState.STARTED));
		assertEquals(true, test.test(ParentState.COMPLETE));
		assertEquals(true, test.test(ParentState.INCOMPLETE));
		assertEquals(true, test.test(ParentState.EXCEPTION));
		
	}
}
