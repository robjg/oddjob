/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.structural.SequentialJob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.OurDirs;
import org.oddjob.tools.StateSteps;

public class ExistsJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(ExistsJobTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("---------------------  " + getName() + "  --------------");
	}
	
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
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		
		oj.destroy();
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
		assertEquals(ParentState.INCOMPLETE, oj.lastStateEvent().getState());
		
		oj.destroy();
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
		assertEquals(ParentState.INCOMPLETE, oj.lastStateEvent().getState());
		
		oj.destroy();
	}
	
	public void testExistsWithFilesExample() {
		
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
		
		assertEquals(ParentState.COMPLETE,
				oddjob.lastStateEvent().getState());
		
		assertEquals(3, console.getLines().length);

		oddjob.destroy();
	}
	
	public void testExistsFilePollingExample() throws IOException, ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		OurDirs dirs = new OurDirs();
		
		File workDir = dirs.relative("work/io");
		
		workDir.mkdir();
		
		File flagFile = new File(workDir, "done.flag");
		
		if (flagFile.exists()) {
			FileUtils.forceDelete(flagFile);
		}
		
		Properties properties = new Properties();
		properties.setProperty("work.dir", workDir.getPath());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/ExistsFilePollingExample.xml", 
				getClass().getClassLoader()));
		oddjob.setProperties(properties);
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
				
		oddjob.load();
		
		SequentialJob sequential = new OddjobLookup(oddjob).lookup(
				"echo-when-file", SequentialJob.class);
		
		StateSteps sequentialStates = new StateSteps(sequential);
		sequentialStates.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.INCOMPLETE);
		
		oddjob.run();
	
		sequentialStates.checkWait();
		
		StateSteps oddjobStates = new StateSteps(oddjob);
		oddjobStates.startCheck(
				ParentState.STARTED, ParentState.COMPLETE);
		
		FileUtils.touch(flagFile);
		
		oddjobStates.checkWait();
		
		console.close();
		console.dump(logger);
		
		assertEquals(ParentState.COMPLETE,
				oddjob.lastStateEvent().getState());
		
		assertEquals(1, console.getLines().length);

		oddjob.destroy();
	}
	
	
	public void testSerialize() throws Exception {
		OurDirs dirs = new OurDirs();
		
		ExistsJob test = new ExistsJob();
		test.setFile(new File(dirs.base(), "test/io/reference/test1.txt"));

		ExistsJob copy = (ExistsJob) OddjobTestHelper.copy(test);
		copy.run();
		
		assertEquals(0, copy.getResult());
	}
}
