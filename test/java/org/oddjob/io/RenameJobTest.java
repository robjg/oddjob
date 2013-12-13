/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.OurDirs;

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
	
	public void testExample() throws Exception {
		File a = new File(dir, "a.txt");
		File b = new File(dir, "b.txt");
		
		FileUtils.touch(a);

		Properties properties = new Properties();
		properties.setProperty("work.dir", dir.getPath());

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/RenameExample.xml",
				getClass().getClassLoader()));
		oddjob.setProperties(properties);
		
		oddjob.load();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Runnable rename1 = lookup.lookup("from", Runnable.class);
		rename1.run();
		
		assertFalse(a.exists());		
		assertTrue(b.exists());		
		
		Runnable rename2 = lookup.lookup("back", Runnable.class);
		rename2.run();
		
		oddjob.destroy();
		
		assertTrue(a.exists());		
		assertFalse(b.exists());		
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
		
		Runnable copy = (Runnable) OddjobTestHelper.copy(test);
		copy.run();

		assertTrue(b.exists());
		assertFalse(a.exists());
	}

}
