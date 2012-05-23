package org.oddjob.launch;

import java.io.File;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

public class PathParserTest extends TestCase {

	private static final Logger logger = Logger.getLogger(
			PathParserTest.class);
	
	String pathToParse = null;
	String result1 = null;
	String result2 = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.info("-----------------" + getName() + "-----------");
	}
	
	void pathSetUp() {
		
		if (";".equals(File.pathSeparator)) {
			logger.info("Windows style path.");
			pathToParse = "c:\\fruit\\apple.jar;c:\\fruit\\orange.jar";
			result1 = "c:\\fruit\\apple.jar";
			result2 = "c:\\fruit\\orange.jar";
		} 
		else if (":".equals(File.pathSeparator)) {
			logger.info("Unix style path.");
			pathToParse = "/local/lib/fruit/apple.jar:/local/lib/fruit/orange.jar";
			result1 = "/local/lib/fruit/apple.jar";
			result2 = "/local/lib/fruit/orange.jar";
		}
		else {
			logger.info("Unknown path style. Test will just succeed.");
		}
	}
	
	
	public void testPathParse() {
		pathSetUp();
		
		if (pathToParse == null) {
			return;
		}
		
		PathParser test = new PathParser();
		
		String[] after = test.processArgs(
				new String[] { 
						"before", 
						"-cp", 
						pathToParse,
						"after"});
		
		assertEquals(2, after.length);
		
		assertEquals("before", after[0]);
		assertEquals("after", after[1]);
		
		String[] results = test.getElements();
		
		assertEquals(2, results.length);
		
		assertEquals(result1, results[0]);
		assertEquals(result2, results[1]);
	}
	
	public void testArgButNoPath() {
		
		PathParser test = new PathParser();
		
		try {
			test.processArgs(new String[] { 
					"before", "-cp" });
			fail("Should fail.");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}
	
	public void testContinue() {
		
		PathParser test = new PathParser();
		
		String[] after =  test.processArgs(new String[] { 
					"--", "-cp" });
		
		assertEquals("--", after[0]);
		assertEquals("-cp", after[1]);
		
		assertEquals(2, after.length);
	}
	
	public void testTwoClassPathsContinue() {
		
		PathParser test = new PathParser();
		
		String[] after =  test.processArgs(new String[] { 
					"-classpath", "foo", "-cp" });
		
		assertEquals("-cp", after[0]);
		
		assertEquals(1, after.length);
	}
}
