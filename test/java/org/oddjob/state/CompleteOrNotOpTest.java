package org.oddjob.state;


import junit.framework.TestCase;

public class CompleteOrNotOpTest extends TestCase {

	public void testVariousStates() {
		
		CompleteOrNotOp test = new CompleteOrNotOp();
		
		assertEquals(JobState.COMPLETE, 
				test.evaluate());
		
		assertEquals(JobState.COMPLETE, 
				test.evaluate(JobState.COMPLETE));
		
		assertEquals(JobState.COMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.COMPLETE, JobState.COMPLETE));
		
		assertEquals(JobState.INCOMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.INCOMPLETE, JobState.COMPLETE));
		
		assertEquals(JobState.COMPLETE, 
				test.evaluate(JobState.READY));

		assertEquals(JobState.EXECUTING, 
				test.evaluate(JobState.EXECUTING));
		
		try {
			test.evaluate(JobState.DESTROYED);
			fail("Should fail.");
		} catch (IllegalArgumentException e) {
			// expected.
		}
	}
	
}
