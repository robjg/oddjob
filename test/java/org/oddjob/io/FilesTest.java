package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class FilesTest extends TestCase {

	private static final Logger logger = Logger.getLogger(FilesTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("-------------------------  " + getName() + 
				"  --------------------------");
	}
	
	public void testExpandRootDirectory() throws IOException {
		
		// Found a root directory
		File roots[] = Files.expand(new File("/*"));
		File file = null;
		for (File possible : roots) {
			if (possible.isDirectory()) {
				file = possible;
			}
		}
		
		if (file == null) {
			throw new RuntimeException("No root directories - how can that be?");
		}
		
		File files[] = Files.expand(file);
		
		logger.info(file + " expands to " + Arrays.toString(files));
		
		assertEquals(1, files.length);
	}
}
