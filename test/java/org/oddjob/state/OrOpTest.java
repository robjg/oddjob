package org.oddjob.state;

import junit.framework.TestCase;

public class OrOpTest extends TestCase {
	
	public void testOr() {
		
		OrStateOp test = new OrStateOp();
		
		assertEquals(JobState.COMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.READY));
		
		assertEquals(JobState.EXCEPTION, 
				test.evaluate(JobState.COMPLETE, JobState.EXCEPTION));	
	}
	

}
