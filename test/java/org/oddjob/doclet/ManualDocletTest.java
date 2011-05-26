package org.oddjob.doclet;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.oddjob.OurDirs;
import org.oddjob.arooa.convert.convertlets.FileConvertlets;
import org.oddjob.doclet.ManualDoclet;
import org.oddjob.oddballs.BuildOddballs;

import junit.framework.TestCase;

public class ManualDocletTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(ManualDocletTest.class);
	
	OurDirs dirs = new OurDirs();
	
	File dest = new File(dirs.base(), "work/reference");
	
	@Override
	protected void setUp() throws Exception {
		logger.info("-------------------  " + getName() + "  -------------------");
		
		if (dest.exists()) {
			logger.info("Deleting " + dest);
			FileUtils.forceDelete(dest);
		}
		
		logger.info("Creating " + dest);
		if (!dest.mkdir()) {
			throw new RuntimeException("Failed to create dir " + dest);
		}
		
	}
	
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
