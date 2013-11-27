/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.oddjob.OddjobTestHelper;
import org.oddjob.Oddjob;
import org.oddjob.OurDirs;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class MkdirJobTest extends TestCase {

	File dir;
	
	public void setUp() throws Exception {
		OurDirs dirs = new OurDirs();
		
		dir = dirs.relative("work/io/mkdir");
		
		if (dir.exists()) {
			FileUtils.forceDelete(dir);
		}
	}

	public void testSimple() {
		MkdirJob test = new MkdirJob();
		test.setDir(dir);
		test.run();

		assertTrue(dir.exists());
	}

	public void testFileExists() throws Exception {
		FileUtils.touch(dir);
		
		MkdirJob test = new MkdirJob();
		test.setDir(dir);
		try {
			test.run();
			fail("Should throw exception.");
		} catch (Exception e) { 
			// expected
		}
			
		assertTrue(dir.isFile());
	}
	
	public void testDirExists() throws Exception {
		FileUtils.forceMkdir(dir);
		
		MkdirJob test = new MkdirJob();
		test.setDir(dir);
		test.run();
			
		assertTrue(dir.isDirectory());
	}
	
	public void testMissingParents() throws Exception {		
		File create = new File(dir, "a/b/c");
		
		MkdirJob test = new MkdirJob();
		test.setDir(create);
		test.run();

		assertTrue(create.exists());
	}
	
	public void testInOddjob() {
		

		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/MkdirExample.xml",
				getClass().getClassLoader()));
		oj.setArgs(new String[] { dir.getPath() });
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		
		assertTrue(new File(dir, "a/b/c").exists());		
	}
	
	public void testSerailize() throws Exception {
		MkdirJob test = new MkdirJob();
		test.setDir(dir);

		Runnable copy = (Runnable) OddjobTestHelper.copy(test);
		copy.run();

		assertTrue(dir.exists());
	}

}
