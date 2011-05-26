package org.oddjob.state;

import junit.framework.TestCase;

public class AndOpTest extends TestCase {

	public void testAnd() {
				
		AndStateOp test = new AndStateOp();
		
		assertEquals(JobState.READY, 
				test.evaluate(JobState.COMPLETE, JobState.INCOMPLETE));
		
		assertEquals(JobState.COMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.COMPLETE));
		
		assertEquals(JobState.EXCEPTION, 
				test.evaluate(JobState.COMPLETE, JobState.EXCEPTION));
		
	}
	
}
