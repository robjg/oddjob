package org.oddjob.state;

import junit.framework.TestCase;

public class AndStateOpTest extends TestCase {

	public void testAndNoStates() {
		
		AndStateOp test = new AndStateOp();
		
		assertEquals(ParentState.READY, test.evaluate());
	}
	
	public void testAndOneStates() {
		
		AndStateOp test = new AndStateOp();
		
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING));	
	}
	
	public void testAndTwoStates() {
				
		AndStateOp test = new AndStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.COMPLETE, JobState.INCOMPLETE));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.COMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.COMPLETE, JobState.EXCEPTION));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.COMPLETE, JobState.EXECUTING));
	}
}
