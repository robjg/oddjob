package org.oddjob.io;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.oddjob.Oddjob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OurDirs;

public class AppendTypeTest extends TestCase {

	File outputDir;

	public void setUp() throws Exception {
		OurDirs dirs = new OurDirs();
		
		outputDir = new File(dirs.base(), "work/io/append");
		
		if (outputDir.exists()) {
			FileUtils.forceDelete(outputDir);
		}
	}

	public void testExample() throws Exception {
		
		FileUtils.forceMkdir(outputDir);
		
		File file = new File(getClass().getResource(
				"AppendExample.xml").getFile());
		
		Oddjob oj = new Oddjob();
		oj.setArgs(new String[] { outputDir.toString() });
		oj.setFile(file);
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

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

}
