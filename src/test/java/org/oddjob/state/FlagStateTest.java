package org.oddjob.state;

import org.junit.Test;

import org.oddjob.OjTestCase;

public class FlagStateTest extends OjTestCase {

	/**
	 * Everything apart from COMPLETE and INCOMPLETE sets state EXCEPTION.
	 */
   @Test
	public void testSettingStateDestoyed() {
		
		FlagState test = new FlagState();
		test.setState(JobState.DESTROYED);
		
		test.run();
		
		assertEquals(JobState.EXCEPTION, test.lastStateEvent().getState());
	}
}
