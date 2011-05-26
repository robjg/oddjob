package org.oddjob.state;

import junit.framework.TestCase;

public class IsContineableTest extends TestCase {

	public void testAllStates() {
		
		IsContinueable test = new IsContinueable();
		
		assertTrue(test.test(JobState.READY));
		assertTrue(test.test(JobState.EXECUTING));
		assertTrue(test.test(JobState.COMPLETE));
		assertFalse(test.test(JobState.INCOMPLETE));
		assertFalse(test.test(JobState.EXCEPTION));
	}
	
	
}
