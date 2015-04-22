package org.oddjob.state;

import junit.framework.TestCase;

public class StandardParentStateConverterTest extends TestCase {

	public void testJobStates() {
		
		ParentStateConverter test = new StandardParentStateConverter();
		
		assertEquals(ParentState.READY, 
				test.toStructuralState(JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.toStructuralState(JobState.EXECUTING));
		
		assertEquals(ParentState.INCOMPLETE, 
				test.toStructuralState(JobState.INCOMPLETE));

		assertEquals(ParentState.COMPLETE, 
				test.toStructuralState(JobState.COMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.toStructuralState(JobState.EXCEPTION));
		
		assertEquals(ParentState.DESTROYED, 
				test.toStructuralState(JobState.DESTROYED));
		
	}

	public void testServiceStates() {
		
		ParentStateConverter test = new StandardParentStateConverter();
		
		assertEquals(ParentState.READY, 
				test.toStructuralState(ServiceState.STARTABLE));
		
		assertEquals(ParentState.ACTIVE, 
				test.toStructuralState(ServiceState.STARTING));
		
		assertEquals(ParentState.STARTED, 
				test.toStructuralState(ServiceState.STARTED));

		assertEquals(ParentState.COMPLETE, 
				test.toStructuralState(ServiceState.STOPPED));
		
		assertEquals(ParentState.EXCEPTION, 
				test.toStructuralState(ServiceState.EXCEPTION));
		
		assertEquals(ParentState.DESTROYED, 
				test.toStructuralState(ServiceState.DESTROYED));
	}
	
	public void testParentStates() {
		
		ParentStateConverter test = new StandardParentStateConverter();
		
		assertEquals(ParentState.READY, 
				test.toStructuralState(ParentState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.toStructuralState(ParentState.EXECUTING));
		
		assertEquals(ParentState.ACTIVE, 
				test.toStructuralState(ParentState.ACTIVE));
		
		assertEquals(ParentState.STARTED, 
				test.toStructuralState(ParentState.STARTED));

		assertEquals(ParentState.COMPLETE, 
				test.toStructuralState(ParentState.COMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.toStructuralState(ParentState.EXCEPTION));
		
		assertEquals(ParentState.DESTROYED, 
				test.toStructuralState(ParentState.DESTROYED));
	}
}
