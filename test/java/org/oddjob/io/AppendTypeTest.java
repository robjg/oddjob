package org.oddjob.io;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.FileUtils;
import org.oddjob.Oddjob;
import org.oddjob.OurDirs;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

import junit.framework.TestCase;

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
		
		Oddjob oj = new Oddjob();
		oj.setArgs(new String[] { outputDir.toString() });
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/AppendExample.xml",
				getClass().getClassLoader()));
		oj.run();
		
		assertEquals(JobState.COMPLETE, oj.lastJobStateEvent().getJobState());

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
