package org.oddjob.jmx.general;

import org.junit.Test;

import java.text.ParseException;

import org.oddjob.jmx.general.MBeanDirectoryPathParser;

import org.oddjob.OjTestCase;

public class MBeanDirectoryPathParserTest extends OjTestCase {

   @Test
	public void testSimpleType() throws ParseException {
		
		String path = "snack:type=fruit,name=apple";
		
		MBeanDirectoryPathParser test = new MBeanDirectoryPathParser();
		
		test.parse(path);
		
		assertEquals(path, test.getName());
		assertEquals(null, test.getProperty());
	}
	
   @Test
	public void testSimpleAttribute() throws ParseException {
		
		String path = "snack:type=fruit,name=apple.vendor";
		
		MBeanDirectoryPathParser test = new MBeanDirectoryPathParser();
		
		test.parse(path);
		
		assertEquals("snack:type=fruit,name=apple", test.getName());
		assertEquals("vendor", test.getProperty());
	}
	
   @Test
	public void testQuotedName() throws ParseException {
		
		String path = "\"snack:type=fruit,name=apple\".vendor";
		
		MBeanDirectoryPathParser test = new MBeanDirectoryPathParser();
		
		test.parse(path);
		
		assertEquals("snack:type=fruit,name=apple", test.getName());
		assertEquals("vendor", test.getProperty());
	}
	
   @Test
	public void testQuotedAll() throws ParseException {
		
		String path = "\"snack:type=fruit,name=apple/vendor\"";
		
		MBeanDirectoryPathParser test = new MBeanDirectoryPathParser();
		
		test.parse(path);
		
		assertEquals("snack:type=fruit,name=apple/vendor", test.getName());
		assertEquals(null, test.getProperty());
	}
	
   @Test
	public void testQuotedAttributeToo() throws ParseException {
		
		String path = "\"snack:type=fruit,name=apple\".\"vendor\"";
		
		MBeanDirectoryPathParser test = new MBeanDirectoryPathParser();
		
		test.parse(path);
		
		assertEquals("snack:type=fruit,name=apple", test.getName());
		assertEquals("\"vendor\"", test.getProperty());
	}
	
   @Test
	public void testMisquotedName() {
		
		String path = "\"snack:type=fruit,name=apple\",vendor";
		
		MBeanDirectoryPathParser test = new MBeanDirectoryPathParser();
		
		try {
			test.parse(path);
			fail("Shouldn't be able to parse.");
		} catch (ParseException e) {
			// Expected.
		}		
	}
}
