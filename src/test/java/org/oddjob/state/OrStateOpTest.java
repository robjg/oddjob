package org.oddjob.state;

import org.junit.Test;

import org.oddjob.OjTestCase;

public class OrStateOpTest extends OjTestCase {
	
   @Test
	public void testOrNoStates() {
		
		OrStateOp test = new OrStateOp();
		
		assertEquals(ParentState.READY, test.evaluate());
	}
	
   @Test
	public void testOrOneStates() {
		
		OrStateOp test = new OrStateOp();
		
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING));
		
	}
   @Test
	public void testOr() {
		
		OrStateOp test = new OrStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY, JobState.READY));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.INCOMPLETE));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.READY));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.COMPLETE, JobState.EXCEPTION));	
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.COMPLETE, JobState.EXECUTING));	
	}
}
