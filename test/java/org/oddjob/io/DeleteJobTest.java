/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.OurDirs;

public class DeleteJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(DeleteJobTest.class);
	
	File dir;

	
	public void setUp() throws Exception {
		logger.debug("----------------" + getName() + "------------------");
		
		OurDirs dirs = new OurDirs();
		
		dir = dirs.relative("work/io/delete");
		
		if (dir.exists()) {
			FileUtils.forceDelete(dir);
		}
	}
	
	public void testDeleteFile() throws Exception {
		FileUtils.forceMkdir(dir);
	
		File testFile = new File(dir, "a");
		FileUtils.touch(testFile);
		
		WildcardSpec wild = new WildcardSpec(new File(dir, "a"));
		File[] found = wild.findFiles(); 
		assertEquals(1, found.length);

		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <delete>" +
			"   <files>" +
			" 	 <file file='" + dir.getPath() + "/a'/>" +
			"   </files>" +
			"  </delete>" +
			" </job>" +
			"</oddjob>";

		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

		assertEquals(false, found[0].exists());
	}
	
	public void testDeleteFiles() throws Exception {
		FileUtils.forceMkdir(dir);
		
		FileUtils.touch(new File(dir, "a"));
		FileUtils.touch(new File(dir, "b"));
		FileUtils.touch(new File(dir, "c"));
		
		WildcardSpec wild = new WildcardSpec(new File(dir, "*"));
		File[] found = wild.findFiles(); 
		assertEquals(3, found.length);


		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/DeleteFilesExample.xml",
				getClass().getClassLoader()));
		
		oj.setArgs(new String[] { dir.getPath().toString() });
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

		found = wild.findFiles();
		assertEquals(0, found.length);
	}
	
	public void testDeleteDir() throws Exception {
		FileUtils.forceMkdir(dir);
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <delete>" +
			"   <files>" +
			"    <file file='" + dir.getPath() + "'/>" +
			"   </files>" +
			"  </delete>" +
			" </job>" +
			"</oddjob>";

		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		assertFalse(dir.exists());
	}
	
	public void testDeleteFullDir() throws Exception {
		FileUtils.forceMkdir(dir);
		
		FileUtils.touch(new File(dir, "a"));
		FileUtils.touch(new File(dir, "b"));
		FileUtils.touch(new File(dir, "c"));
		

		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <delete>" +
			"   <files>" +
			"    <file file='" + dir.getPath() + "'/>" +
			"   </files>" +
			"  </delete>" +
			" </job>" +
			"</oddjob>";

		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		oj.run();
		assertEquals(ParentState.EXCEPTION, oj.lastStateEvent().getState());
		
		assertTrue(dir.exists());

		xml = "<oddjob>" +
				"<job>" +
				" <delete force='true'>" +
				"  <files>" +
				"   <file file='" + dir.getPath() + "'/>" +
				"  </files>" +
				" </delete>" +
				"</job>" +
				"</oddjob>";
		
		oj.hardReset();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));

		oj.run();
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

		assertFalse(dir.exists());
		
		oj.destroy();
	}
	
	public void testSerialize() throws Exception {
		FileUtils.forceMkdir(dir);

		assertTrue(dir.exists());
		DeleteJob test = new DeleteJob();
		test.setFiles(new File[] { dir } );

		Callable<Integer> copy = 
				(Callable<Integer>) OddjobTestHelper.copy(test);
		copy.call();
		
		assertFalse(dir.exists());
	}
	
	public void testReallyRoot() throws IOException, InterruptedException {
		
		File file = new File("/");
		
		// This is a really dangerous test so lets make sure wherever it's
		// being run File behaves as expected.
		assertEquals(true, file.getCanonicalFile().isAbsolute());
		assertEquals(null, file.getCanonicalFile().getParentFile());
		
		DeleteJob test = new DeleteJob();
		test.setFiles(new File[] { file });
		
		try {
			test.call();
			fail("Should fail.");
		}
		catch (RuntimeException e) {
			assertEquals("You can not delete root (/*) files without setting the reallyRoot property to true.",
					e.getMessage());
		}
		
		assertEquals(1, test.getErrorCount());
		
		test.reset();
		
		file = new File("/a");
		test.setFiles(new File[] { file });
		
		assertEquals(null, file.getParentFile().getParentFile());
		
		try {
			test.call();
			fail("Should fail.");
		}
		catch (RuntimeException e) {
			assertEquals("You can not delete root (/*) files without setting the reallyRoot property to true.",
					e.getMessage());
		}
		
		assertEquals(1, test.getErrorCount());
		
		test.reset();
		
		file = new File("/a/../b");
		test.setFiles(new File[] { file });
		
		try {
			test.call();
			fail("Should fail.");
		}
		catch (RuntimeException e) {
			assertEquals("You can not delete root (/*) files without setting the reallyRoot property to true.",
					e.getMessage());
		}
		
		assertEquals(1, test.getErrorCount());
	}
}
