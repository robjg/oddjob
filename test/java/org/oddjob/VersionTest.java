package org.oddjob;

import java.text.ParseException;

import org.oddjob.arooa.utils.DateHelper;

import junit.framework.TestCase;

public class VersionTest extends TestCase {
	
	public void testToString() throws ParseException {
		
		Version test = new Version(1, 2, 3);
		
		assertEquals("1.2.3", test.toString());

		test = new Version(1, 2, 3, true, null);
		
		assertEquals("1.2.3-SNAPSHOT", test.toString());
		
		test = new Version(1, 2, 3, true, DateHelper.parseDateTime("2013-09-08 09:45"));
		
		assertEquals("1.2.3-SNAPSHOT 2013-09-08 09:45:00", test.toString());
	}

	public void testCreating() {
		
		Version test = Version.versionFor("1.2.3", null);
		
		assertEquals("1.2.3", test.toString());
		
		test = Version.versionFor("1.2.3-SNAPSHOT", null);
		
		assertEquals("1.2.3-SNAPSHOT", test.toString());
		
		test = Version.versionFor("1.2.3", "2013-09-08 09:45");
		
		assertEquals("1.2.3 2013-09-08 09:45:00", test.toString());
		
		test = Version.versionFor("1.2.3", "2013-09-08 09:45");
		
		assertEquals("1.2.3 2013-09-08 09:45:00", test.toString());
		
		test = Version.versionFor("one.two.three", "2013-09-08 09:45");
		
		assertEquals(null, test);

		test = Version.versionFor("1.2.3", "tuesday");
		
		assertEquals("1.2.3", test.toString());
	}
	
	public void tesComparison() {
		
		Version test1 = new Version(1, 2, 3);
		
		Version test2 = new Version(2, 2, 3);
		
		assertEquals(true, test1.compareTo(test2) < 0);
		assertEquals(true, test2.compareTo(test1) > 0);
		
		test1 = new Version(1, 2, 3);
		
		test2 = new Version(1, 3, 3);
		
		assertEquals(true, test1.compareTo(test2) < 0);
		assertEquals(true, test2.compareTo(test1) > 0);
		
		test1 = new Version(1, 2, 3);
		
		test2 = new Version(1, 2, 4);
		
		assertEquals(true, test1.compareTo(test2) < 0);
		assertEquals(true, test2.compareTo(test1) > 0);
		
		test1 = new Version(1, 2, 3);
		
		test2 = new Version(1, 2, 3);
		
		assertEquals(0, test1.compareTo(test2));
		assertEquals(0, test2.compareTo(test1));
	}
	
	public void testEquals() {
		
		Version test1 = new Version(1, 2, 3);
		
		Version test2 = new Version(1, 2, 3);
		
		assertEquals(true, test1.equals(test2));
		assertEquals(true, test1.hashCode() == test2.hashCode());
	}
}
