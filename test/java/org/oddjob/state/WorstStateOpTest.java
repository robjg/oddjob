package org.oddjob.state;

import junit.framework.TestCase;

public class WorstStateOpTest extends TestCase {

	public void testEvaluateSingleOp() {
		
		WorstStateOp test = new WorstStateOp();
		
		assertEquals(JobState.READY, 
				test.evaluate(JobState.READY));
		
		assertEquals(JobState.EXECUTING, 
				test.evaluate(JobState.EXECUTING));
		
		assertEquals(JobState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION));
				
		assertEquals(JobState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE));
				
		assertEquals(JobState.COMPLETE, 
				test.evaluate(JobState.COMPLETE));
		
	}
	
	public void testEvaluateTwoOps() {
		
		WorstStateOp test = new WorstStateOp();
		
		assertEquals(JobState.READY, 
				test.evaluate(JobState.READY, JobState.READY));
		
		assertEquals(JobState.EXECUTING, 
				test.evaluate(JobState.READY, JobState.EXECUTING));
		
		assertEquals(JobState.READY, 
				test.evaluate(JobState.READY, JobState.COMPLETE));

		assertEquals(JobState.INCOMPLETE, 
				test.evaluate(JobState.READY, JobState.INCOMPLETE));
		
		assertEquals(JobState.EXCEPTION, 
				test.evaluate(JobState.READY, JobState.EXCEPTION));
		
		assertEquals(JobState.EXECUTING, 
				test.evaluate(JobState.EXECUTING, JobState.READY));
		
		assertEquals(JobState.EXECUTING, 
				test.evaluate(JobState.EXECUTING, JobState.EXECUTING));
		
		assertEquals(JobState.EXECUTING, 
				test.evaluate(JobState.EXECUTING, JobState.COMPLETE));

		assertEquals(JobState.EXECUTING, 
				test.evaluate(JobState.EXECUTING, JobState.INCOMPLETE));
		
		assertEquals(JobState.EXECUTING, 
				test.evaluate(JobState.EXECUTING, JobState.EXCEPTION));
		
		assertEquals(JobState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.READY));
		
		assertEquals(JobState.EXECUTING, 
				test.evaluate(JobState.INCOMPLETE, JobState.EXECUTING));
		
		assertEquals(JobState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.COMPLETE));

		assertEquals(JobState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.INCOMPLETE));
		
		assertEquals(JobState.EXCEPTION, 
				test.evaluate(JobState.INCOMPLETE, JobState.EXCEPTION));
		
		assertEquals(JobState.READY, 
				test.evaluate(JobState.COMPLETE, JobState.READY));
		
		assertEquals(JobState.EXECUTING, 
				test.evaluate(JobState.COMPLETE, JobState.EXECUTING));
		
		assertEquals(JobState.COMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.COMPLETE));

		assertEquals(JobState.INCOMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.INCOMPLETE));
		
		assertEquals(JobState.EXCEPTION, 
				test.evaluate(JobState.COMPLETE, JobState.EXCEPTION));
		
		assertEquals(JobState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.READY));
		
		assertEquals(JobState.EXECUTING, 
				test.evaluate(JobState.EXCEPTION, JobState.EXECUTING));
		
		assertEquals(JobState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.COMPLETE));

		assertEquals(JobState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.INCOMPLETE));
		
		assertEquals(JobState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.EXCEPTION));
	}
	
	public void testDestroyed() {

		
		WorstStateOp test = new WorstStateOp();
		
		try {
			assertEquals(JobState.DESTROYED, 
					test.evaluate(JobState.DESTROYED, JobState.DESTROYED));
			fail("Should fail");
		} catch (IllegalStateException e) {
			// expected.
		}
	}
}
