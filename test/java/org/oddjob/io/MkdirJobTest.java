/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;
import org.junit.Before;

import org.junit.Test;

import java.io.File;

import org.oddjob.OjTestCase;

import org.apache.commons.io.FileUtils;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.OurDirs;

public class MkdirJobTest extends OjTestCase {

	File dir;
	
   @Before
   public void setUp() throws Exception {
		OurDirs dirs = new OurDirs();
		
		dir = dirs.relative("work/io/mkdir");
		
		if (dir.exists()) {
			FileUtils.forceDelete(dir);
		}
	}

   @Test
	public void testSimple() {
		MkdirJob test = new MkdirJob();
		test.setDir(dir);
		test.run();

		assertTrue(dir.exists());
	}

   @Test
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
	
   @Test
	public void testDirExists() throws Exception {
		FileUtils.forceMkdir(dir);
		
		MkdirJob test = new MkdirJob();
		test.setDir(dir);
		test.run();
			
		assertTrue(dir.isDirectory());
	}
	
   @Test
	public void testMissingParents() throws Exception {		
		File create = new File(dir, "a/b/c");
		
		MkdirJob test = new MkdirJob();
		test.setDir(create);
		test.run();

		assertTrue(create.exists());
	}
	
   @Test
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
	
   @Test
	public void testSerailize() throws Exception {
		MkdirJob test = new MkdirJob();
		test.setDir(dir);

		Runnable copy = (Runnable) OddjobTestHelper.copy(test);
		copy.run();

		assertTrue(dir.exists());
	}

}
