package org.oddjob.doclet;
import org.junit.Before;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.arooa.convert.convertlets.FileConvertlets;
import org.oddjob.doclet.ManualDoclet;
import org.oddjob.oddballs.BuildOddballs;
import org.oddjob.tools.OurDirs;

import org.oddjob.OjTestCase;

public class ManualDocletTest extends OjTestCase {
	
	private static final Logger logger = LoggerFactory.getLogger(ManualDocletTest.class);
	
	OurDirs dirs = new OurDirs();
	
	File dest = new File(dirs.base(), "work/reference");
	
    @Before
    public void setUp() throws Exception {
		logger.info("-------------------  " + getName() + "  -------------------");
		
		// try 3 times - why does this fail?
		for (int i = 0; ; ++i) {
			if (dest.exists()) {
				logger.info("Deleting " + dest);
				try {
					FileUtils.forceDelete(dest);
				} catch (IOException e) {
					if (i < 3) {
						logger.error("failed deleting " + dest, e);
						Thread.sleep(200);
						continue;
					}
					else {
						throw e;
					}
				}
			}
			break;
		}
		
		logger.info("Creating " + dest);
		if (!dest.mkdir()) {
			throw new RuntimeException("Failed to create dir " + dest);
		}
		
	}
	
   @Test
	public void testStart() {

		File index = new File(dest, "index.html");
		File oddjob = new File(dest, "org/oddjob/Oddjob.html");
		
		int result = com.sun.tools.javadoc.Main.execute(
				new String[] {
						"-doclet", ManualDoclet.class.getName(),
						"-sourcepath", dirs.base() + "/src/java", 
						"-d", dest.toString(), 
						"-private",
						"org.oddjob"} );
		
		assertEquals(0, result);
		
		assertTrue(index.exists());
		assertTrue(oddjob.exists());
	}
	
   @Test
	public void testIstType() {

		File src = new File(dirs.base(), "build/src");
		if (!src.exists()) {
			return;
		}
		
		File index = new File(dest, "index.html");
		File is = new File(dest, "org/oddjob/arooa/types/IsType.html");
		
		int result = com.sun.tools.javadoc.Main.execute(
				new String[] {
						"-doclet", ManualDoclet.class.getName(),
						"-sourcepath", dirs.base() + "/build/src", 
						"-d", dest.toString(), 
						"org.oddjob.arooa.types"} );
		
		assertEquals(0, result);
		
		assertTrue(index.exists());
		assertTrue(is.exists());
	}
	
	
   @Test
	public void testDescriptorPath() {

		OurDirs dirs = new OurDirs();
		
		File src = new File(dirs.base(), "build/src");
		if (!src.exists()) {
			return;
		}
		
		new BuildOddballs().run();
		
		
		File index = new File(dest, "index.html");
		File apple = new File(dest, "fruit/Apple.html");
		File is = new File(dest, "org/oddjob/arooa/types/IsType.html");
		
		String sourcePath = new FileConvertlets().filesToPath(
				new File[] { dirs.relative("test/oddballs/apple/src"),
						dirs.relative("test/oddballs/orange/src")});
		
		String descriptorPath = new FileConvertlets().filesToPath(
				new File[] { dirs.relative("test/oddballs/apple/classes"),
						dirs.relative("test/oddballs/orange/classes")});
		
		int result = com.sun.tools.javadoc.Main.execute(
				new String[] {
						"-doclet", ManualDoclet.class.getName(),
						"-sourcepath", sourcePath, 
						"-d", dest.toString(), 
						"-dp", descriptorPath, 
						"fruit"} );
		
		assertEquals(0, result);
		
		assertTrue(index.exists());
		assertTrue(apple.exists());
		assertFalse(is.exists());
	}	
}
