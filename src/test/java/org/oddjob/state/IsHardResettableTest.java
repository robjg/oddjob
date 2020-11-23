package org.oddjob.state;

import org.junit.Test;
import org.oddjob.OjTestCase;

public class IsHardResettableTest extends OjTestCase {

   @Test
	public void testJobStates() {
		
		StateCondition test = new IsHardResetable();
		
		assertEquals(true, test.test(JobState.READY));
		assertEquals(false, test.test(JobState.EXECUTING));
		assertEquals(true, test.test(JobState.COMPLETE));
		assertEquals(true, test.test(JobState.INCOMPLETE));
		assertEquals(true, test.test(JobState.EXCEPTION));
	}
	
   @Test
	public void testServiceStates() {
		
		StateCondition test = new IsHardResetable();
		
		assertEquals(true, test.test(ServiceState.STARTABLE));
		assertEquals(false, test.test(ServiceState.STARTING));
		assertEquals(false, test.test(ServiceState.STARTED));
		assertEquals(true, test.test(ServiceState.EXCEPTION));
		assertEquals(true, test.test(ServiceState.STOPPED));
	}
	
   @Test
	public void testParentStates() {
		
		StateCondition test = new IsHardResetable();
		
		assertEquals(true, test.test(ParentState.READY));
		assertEquals(false, test.test(ParentState.EXECUTING));
		assertEquals(false, test.test(ParentState.STARTED));
		assertEquals(false, test.test(ParentState.ACTIVE));
		assertEquals(true, test.test(ParentState.COMPLETE));
		assertEquals(true, test.test(ParentState.INCOMPLETE));
		assertEquals(true, test.test(ParentState.EXCEPTION));
	}	
}
