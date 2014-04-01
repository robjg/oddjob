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
	
	public void testRootFiles() throws IOException {
		
		File file = new File("/Users");
		
		File files[] = Files.expand(file);
		
		logger.info(file + " expands to " + Arrays.toString(files));
		
		assertEquals(1, files.length);
	}
}
