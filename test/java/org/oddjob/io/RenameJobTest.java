/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OurDirs;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class RenameJobTest extends TestCase {

	File dir;
	
	public void setUp() throws Exception {
		OurDirs dirs = new OurDirs();
		dir = dirs.relative("work/io/rename");
		
		if (dir.exists()) {
			FileUtils.forceDelete(dir);
		}
		FileUtils.forceMkdir(dir);
	}

	public void testSimple() throws Exception {
		File a = new File(dir, "a");
		File b = new File(dir, "b");
		
		FileUtils.touch(a);
		
		assertFalse(b.exists());
		assertTrue(a.exists());
		
		RenameJob test = new RenameJob();
		test.setFrom(a);
		test.setTo(b);
		test.run();

		assertTrue(b.exists());
		assertFalse(a.exists());
	}

	public void testDir() throws Exception {
		File a = new File(dir, "a");
		File b = new File(dir, "b");
		
		FileUtils.forceMkdir(a);
		
		RenameJob test = new RenameJob();
		test.setFrom(a);
		test.setTo(b);
		test.run();

		assertTrue(b.exists());
		assertFalse(a.exists());
	}
	
	public void testInOddjob() throws Exception {
		File a = new File(dir, "a");
		File b = new File(dir, "b");
		
		FileUtils.touch(a);
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <rename from='" + dir.getPath() + "/a' to='" + dir.getPath() + "/b'/>" +
			" </job>" +
			"</oddjob>";

		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		assertTrue(b.exists());		
	}
	
	public void testSerialize() throws Exception {
		File a = new File(dir, "a");
		File b = new File(dir, "b");
		
		FileUtils.touch(a);
		
		assertFalse(b.exists());
		assertTrue(a.exists());
		
		RenameJob test = new RenameJob();
		test.setFrom(a);
		test.setTo(b);
		
		Runnable copy = (Runnable) Helper.copy(test);
		copy.run();

		assertTrue(b.exists());
		assertFalse(a.exists());
	}

	
}
