package org.oddjob.launch;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.oddjob.OjTestCase;

public class ClassPathHelperTest extends OjTestCase {


   @Test
	public void testAll() throws IOException {

		File a = new File("a.jar");
		File b = new File("b.jar");
		
		ClassPathHelper test = new ClassPathHelper(
				new File[] { a, b});
		
		String cp = System.getProperty(ClassPathHelper.CLASS_PATH_PROPERTY);
		
		test.appendToJavaClassPath();
		
		assertTrue(cp.length() > 0);
		
		assertEquals(cp + File.pathSeparator + b.getCanonicalPath(), 
				System.getProperty(ClassPathHelper.CLASS_PATH_PROPERTY));
		
		System.setProperty(ClassPathHelper.CLASS_PATH_PROPERTY, cp);
		
		URL[] urls = test.toURLs();
		assertEquals(a.toURI().toURL(), urls[0]);
		assertEquals(b.toURI().toURL(), urls[1]);
		
		String asString = test.toString();
		assertEquals("a.jar" + File.pathSeparator + "b.jar", asString);
	}
}
