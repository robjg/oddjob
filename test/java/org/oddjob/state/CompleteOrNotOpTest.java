package org.oddjob.state;

import org.junit.Test;


import org.oddjob.OjTestCase;

public class CompleteOrNotOpTest extends OjTestCase {

   @Test
	public void testVariousStates() {
		
		CompleteOrNotOp test = new CompleteOrNotOp();
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate());
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.COMPLETE, JobState.COMPLETE));
		
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.INCOMPLETE, JobState.COMPLETE));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.READY));

		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING));
		
		try {
			test.evaluate(JobState.DESTROYED);
			fail("Should fail.");
		} catch (IllegalStateException e) {
			// expected.
		}
	}
	
}
