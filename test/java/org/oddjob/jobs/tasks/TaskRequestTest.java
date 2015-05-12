package org.oddjob.jobs.tasks;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;

public class TaskRequestTest extends TestCase {

	private static final Logger logger = Logger.getLogger(TaskRequestTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("--------------------  " + getName() + "  ------------------");
	}
	
	public void testSimpleExample() {
		
		File file = new File(getClass().getResource("TaskRequestSimple.xml"
				).getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		ConsoleCapture capture = new ConsoleCapture();
		capture.capture(Oddjob.CONSOLE);
		
		oddjob.run();
		
		assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());
		
		capture.close();
		
		String[] lines = capture.getLines();
		
		assertEquals("Hello Rod.", lines[0].trim());
		assertEquals("Hello Jane.", lines[1].trim());
		assertEquals("Hello Freddy.", lines[2].trim());

		oddjob.destroy();
	}
	
}
