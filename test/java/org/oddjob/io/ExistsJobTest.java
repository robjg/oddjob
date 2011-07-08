/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConsoleCapture;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OurDirs;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

public class ExistsJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(ExistsJobTest.class);
	
	public void testFile() {
		OurDirs dirs = new OurDirs();
		
		ExistsJob test = new ExistsJob();
		test.setFile(new File(dirs.base(), "test/io/reference/test1.txt"));
		
		assertEquals(-1L, test.getSize());
		assertNull(test.getLastModified());
		assertEquals(-1, test.getResult());
		
		test.run();
		
		assertTrue(test.getSize() > 0);
		assertNotNull(test.getLastModified());
		assertEquals(0, test.getResult());
	}
	
	public void testWild() {
		OurDirs dirs = new OurDirs();
		
		ExistsJob test = new ExistsJob();
		test.setFile(new File(dirs.base(), "test/io/reference/*.txt"));
		test.run();
		assertEquals(0, test.getResult());
		
		assertEquals(3, test.getExists().length);
		
		assertEquals(-1L, test.getSize());
		assertNull(test.getLastModified());		
	}
	
	public void testInOddjob() {
		
		OurDirs dirs = new OurDirs();
		
		Oddjob oj = new Oddjob();
		oj.setArgs(new String[] { dirs.base().toString() } );
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/ExistsSimpleExample.xml",
				getClass().getClassLoader()));

		oj.run();
		assertEquals(JobState.COMPLETE, oj.lastJobStateEvent().getJobState());
	}
	
	public void testInOddjob2() {
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <exists file='idontexist.noway'/>" +
			" </job>" +
			"</oddjob>";
				
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));

		oj.run();
		assertEquals(JobState.INCOMPLETE, oj.lastJobStateEvent().getJobState());
	}
	
	public void testExistsResultsExample() {

		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <exists file='idontexist.noway'/>" +
			" </job>" +
			"</oddjob>";
				
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));

		oj.run();
		assertEquals(JobState.INCOMPLETE, oj.lastJobStateEvent().getJobState());
	}
	
	public void existsWithFilesExamplesTest() {
		
		OurDirs dirs = new OurDirs();
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/ExistsWithFilesExample.xml", 
				getClass().getClassLoader()));
		oddjob.setArgs(new String[] { dirs.base().toString() } );
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
				
		oddjob.run();
		
		console.close();
		console.dump(logger);
		
		assertEquals(JobState.COMPLETE,
				oddjob.lastJobStateEvent().getJobState());
		
		assertEquals(2, console.getLines().length);
		
	}
	
	
	public void testSerialize() throws Exception {
		OurDirs dirs = new OurDirs();
		
		ExistsJob test = new ExistsJob();
		test.setFile(new File(dirs.base(), "test/io/reference/test1.txt"));

		ExistsJob copy = (ExistsJob) Helper.copy(test);
		copy.run();
		
		assertEquals(0, copy.getResult());
	}
}
