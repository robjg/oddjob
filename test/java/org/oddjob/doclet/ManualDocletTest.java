package org.oddjob.doclet;

import java.io.File;

import org.oddjob.OurDirs;
import org.oddjob.doclet.ManualDoclet;

import junit.framework.TestCase;

public class ManualDocletTest extends TestCase {
	
	public void testStart() {

		OurDirs dirs = new OurDirs();
		
		File dest = new File(dirs.base(), "work/reference");
		
		File index = new File(dest, "index.html");
		File oddjob = new File(dest, "org/oddjob/Oddjob.html");
		
		dest.mkdir();
		
		if (index.exists()) {
			assertTrue(index.delete());
		}
		
		if (oddjob.exists()) {
			assertTrue(oddjob.delete());
		}
		
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

		OurDirs dirs = new OurDirs();
		
		File src = new File(dirs.base(), "build/src");
		if (!src.exists()) {
			return;
		}
		
		File dest = new File(dirs.base(), "work/reference");
		
		File index = new File(dest, "index.html");
		File is = new File(dest, "org/oddjob/arooa/types/IsType.html");
		
		dest.mkdir();
		
		if (index.exists()) {
			assertTrue(index.delete());
		}
		if (is.exists()) {
			assertTrue(is.delete());
		}
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
	
	
	
}
