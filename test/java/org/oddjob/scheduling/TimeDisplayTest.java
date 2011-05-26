package org.oddjob.scheduling;

import junit.framework.TestCase;

public class TimeDisplayTest extends TestCase {

	public void testLotsOfTimes() {
		
		assertEquals("1,000 00:00:00:000", new TimeDisplay(1000L * 24 * 60 * 60 * 1000).toString());
		
		assertEquals("0 00:02:03:456", new TimeDisplay(123456).toString());
	}
}
