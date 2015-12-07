package org.oddjob.io;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.FragmentHelper;

public class StderrTypeTest extends TestCase {

	private static final Logger logger = Logger.getLogger(StderrTypeTest.class);
	
	@Override
	protected void setUp() throws Exception {
		logger.debug("-------------------  " + getName() + "  --------------");
	}
	
	
	String EOL = System.getProperty("line.separator");
	
	public void testStderrInOddjob() {
		
		String xml =
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <copy>" +
			"     <input>" +
			"      <buffer>Hello" + EOL + "</buffer>" + 
			"     </input>" + 
			"     <output>" +
			"      <stderr/>" +
			"     </output>" +
			"    </copy>" +
			"    <copy>" +
			"     <input>" +
			"      <buffer>World" + EOL + "</buffer>" + 
			"     </input>" + 
			"     <output>" +
			"      <stderr/>" +
			"     </output>" +
			"    </copy>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		ConsoleCapture results = new ConsoleCapture();
		try (ConsoleCapture.Close close = results.captureConsole()) {
			
			oddjob.run();
		}
		
		oddjob.destroy();
		
		results.dump(logger);
		
		String[] lines = results.getLines();
				
		assertEquals("Hello", lines[0].trim());
		assertEquals("World", lines[1].trim());
	}
	
	public void testExample() throws ArooaParseException {

		FragmentHelper helper = new FragmentHelper();
		
		Runnable copy = (Runnable) helper.createComponentFromResource(
				"org/oddjob/io/StderrTypeExample.xml");
		
		ConsoleCapture results = new ConsoleCapture();
		try (ConsoleCapture.Close close = results.captureConsole()) {
			
			copy.run();
		}
		
		String[] lines = results.getLines();
		
		assertEquals("It's all going wrong!", lines[0].trim());
		assertEquals(1, lines.length);
	}
}
