package org.oddjob.oddballs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import org.oddjob.tools.OurDirs;

import junit.framework.TestCase;

public class DirectoryOddball2Test extends TestCase {

	
	public void testURLs() throws IOException {
		
		DirectoryOddball test = new DirectoryOddball();
		
		OurDirs dirs = new OurDirs();
		
		URL[] urls = test.classpathURLs(dirs.base());
		
		HashSet<URL> set = new HashSet<URL>();
		set.addAll(Arrays.asList(urls));
		
		assertTrue(set.contains(
				new File(dirs.base(), 
						"lib/commons-logging-1.1.1.jar").getCanonicalFile().toURI().toURL()));
	}
}
