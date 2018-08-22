package org.oddjob.io;
import org.junit.Before;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

import org.oddjob.OjTestCase;

import org.apache.commons.io.FileUtils;
import org.oddjob.Oddjob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OurDirs;

public class AppendTypeTest extends OjTestCase {

	File outputDir;

   @Before
   public void setUp() throws Exception {
		OurDirs dirs = new OurDirs();
		
		outputDir = new File(dirs.base(), "work/io/append");
		
		if (outputDir.exists()) {
			FileUtils.forceDelete(outputDir);
		}
	}

   @Test
	public void testExample() throws Exception {
		
		FileUtils.forceMkdir(outputDir);
		
		File file = new File(getClass().getResource(
				"AppendExample.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setArgs(new String[] { outputDir.toString() });
		oddjob.setFile(file);
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

		File resultFile = new File(outputDir, "messages.txt");
		
		assertTrue(resultFile.exists());
				
		BufferType buffer = new BufferType();
		buffer.configured();
		
		CopyJob copy = new CopyJob();
		copy.setInput(new FileInputStream(resultFile));
		copy.setOutput(buffer.toOutputStream());
		
		copy.run();
		
		String[] lines = buffer.getLines();
		
		assertEquals(2, lines.length);
		
		assertEquals("Hello World", lines[0]);
		assertEquals("Goodbye World", lines[1]);
	}

   @Test
	public void testAppendWithTeeType() throws Exception {
		
		FileUtils.forceMkdir(outputDir);
		
		File file = new File(getClass().getResource(
				"AppendWithTeeType.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setArgs(new String[] { outputDir.toString() });
		oddjob.setFile(file);
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

		File resultFile = new File(outputDir, "messages.txt");
		
		assertTrue(resultFile.exists());
				
		BufferType buffer = new BufferType();
		buffer.configured();
		
		CopyJob copy = new CopyJob();
		copy.setInput(new FileInputStream(resultFile));
		copy.setOutput(buffer.toOutputStream());
		
		copy.run();
		
		String[] lines = buffer.getLines();
		
		assertEquals("Hello World", lines[0]);
		assertEquals("Goodbye World", lines[1]);
		
		assertEquals(2, lines.length);
		
		oddjob.destroy();
	}
}
