package org.oddjob.oddballs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;
import org.oddjob.OjTestCase;

public class DirectoryOddball2Test extends OjTestCase {

	
    @Test
	public void testURLs() throws IOException {
		
		DirectoryOddball test = new DirectoryOddball();
		
		File javaHome = new File(System.getProperty("java.home"));
				
		URL[] urls = test.classpathURLs(javaHome);
		
		HashSet<URL> set = new HashSet<URL>();
		set.addAll(Arrays.asList(urls));
		
		assertTrue(set.contains(
				new File(javaHome, "lib/rt.jar").getCanonicalFile().toURI().toURL()));
	}
}
