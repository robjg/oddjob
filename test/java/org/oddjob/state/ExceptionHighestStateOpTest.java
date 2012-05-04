package org.oddjob.state;

import junit.framework.TestCase;

public class ExceptionHighestStateOpTest extends TestCase {

	public void testEvaluateSingleOp() {
		
		ExceptionHighestStateOp test = new ExceptionHighestStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION));
				
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE));
				
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE));
		
	}
	
	public void testEvaluateTwoOps() {
		
		ExceptionHighestStateOp test = new ExceptionHighestStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.READY, JobState.EXECUTING));
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.READY, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.READY, JobState.EXCEPTION));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING, JobState.EXECUTING));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING, JobState.COMPLETE));

		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXECUTING, JobState.EXCEPTION));
		
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.INCOMPLETE, JobState.EXECUTING));
		
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.INCOMPLETE, JobState.EXCEPTION));
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.COMPLETE, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.COMPLETE, JobState.EXECUTING));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.COMPLETE, JobState.EXCEPTION));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.READY));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.EXECUTING));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.COMPLETE));

		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.EXCEPTION));
	}
	
	public void testDestroyed() {
		
		ExceptionHighestStateOp test = new ExceptionHighestStateOp();
		
		try {
			assertEquals(JobState.DESTROYED, 
					test.evaluate(JobState.DESTROYED, JobState.DESTROYED));
			fail("Should fail");
		} catch (IllegalStateException e) {
			// expected.
		}
	}
}
