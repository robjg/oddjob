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
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.OurDirs;

public class CopyJobTest extends OjTestCase {

	File reference;
	File dir;

   @Before
   public void setUp() throws Exception {
		OurDirs dirs = new OurDirs();
		
		reference = new File(dirs.base(), "test/io/reference");
		dir = new File(dirs.base(), "work/io/copy");
		
		if (dir.exists()) {
			FileUtils.forceDelete(dir);
		}
	}

   @Test
	public void testCopyFile() throws Exception {
		FileUtils.forceMkdir(dir);
		
		OurDirs dirs = new OurDirs();
		
		Oddjob oj = new Oddjob();
		oj.setArgs(new String[] { dirs.base().toString() });
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/CopyFileExample.xml",
				getClass().getClassLoader()));
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

		assertTrue(new File(dir, "test1.txt").exists());
	}

   @Test
	public void testCopyFiles() throws Exception {
		FileUtils.forceMkdir(dir);
		
		String xml = 
			"<oddjob id='this'>" +
			" <job>" +
			"  <copy to='${this.args[0]}/work/io/copy'>" +
			"   <from>" +
			"    <files files='${this.args[0]}/test/io/reference/*.txt'/>" +
			"   </from>" +
			"  </copy>" +
			" </job>" +
			"</oddjob>";

		OurDirs dirs = new OurDirs();
		
		Oddjob oj = new Oddjob();
		oj.setArgs(new String[] { dirs.base().toString() });
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

		assertEquals(3, new WildcardSpec(
				new File(dir, "*.txt")).findFiles().length);
	}

   @Test
	public void testCopyDirectory() throws Exception {
		FileUtils.forceMkdir(dir);
		
		OurDirs dirs = new OurDirs();
		
		Oddjob oj = new Oddjob();
		oj.setArgs(new String[] { dirs.base().toString() });
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/CopyDirectory.xml", 
				getClass().getClassLoader()));
		
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

		assertTrue(new File(dir, "a/x/test3.txt").exists());
	}

   @Test
	public void testCopyDirectory2() throws Exception {
		// directory doesn't exist this time.
		// dir.mkdir();
		

		OurDirs dirs = new OurDirs();
		
		Oddjob oj = new Oddjob();
		oj.setArgs(new String[] { dirs.base().toString() });
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/CopyDirectory.xml", 
				getClass().getClassLoader()));
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

		assertTrue(new File(dir, "x/test3.txt").exists());
	}
	
   @Test
	public void testSerialize() throws Exception {
		dir.mkdir();

		OurDirs dirs = new OurDirs();
		
		CopyJob test = new CopyJob();
		test.setFrom(new File[] {
				new File(dirs.base(), "test/io/reference/test1.txt") });
		test.setTo(new File(dirs.base(), "work/io/copy"));

		Runnable copy = (Runnable) OddjobTestHelper.copy(test);
		copy.run();
		
		assertTrue(new File(dir, "test1.txt").exists());
	}

   @Test
	public void testCopyBuffer() throws ArooaPropertyException, ArooaConversionException {
		
		OurDirs dirs = new OurDirs();
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/CopyFileToBuffer.xml",
				getClass().getClassLoader()));
		
		oddjob.setArgs(new String[] { dirs.base().toString() });
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
				
		String result = new OddjobLookup(oddjob).lookup("e.text", String.class);
		
		assertEquals("Test 1", result.trim());
		
		oddjob.destroy();
	}
}
