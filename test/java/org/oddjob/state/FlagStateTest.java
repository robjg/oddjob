package org.oddjob.state;

import junit.framework.TestCase;

public class FlagStateTest extends TestCase {

	/**
	 * Everything apart from COMPLETE and INCOMPLETE sets state EXCEPTION.
	 */
	public void testSettingStateDestoyed() {
		
		FlagState test = new FlagState();
		test.setState(JobState.DESTROYED);
		
		test.run();
		
		assertEquals(JobState.EXCEPTION, test.lastJobStateEvent().getJobState());
	}
}
