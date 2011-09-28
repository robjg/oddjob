package org.oddjob.io;

import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.LoggingPrintStream;

public class StdoutTest extends TestCase {

	private static final Logger logger = Logger.getLogger(StdoutTest.class);
	
	@Override
	protected void setUp() throws Exception {
		logger.debug("-------------------  " + getName() + "  --------------");
	}
	
	
	class Console implements LogListener {
		
		StringBuilder out = new StringBuilder();
		
		@Override
		public void logEvent(LogEvent logEvent) {
			out.append(logEvent.getMessage());
		}
	}
	
	String EOL = System.getProperty("line.separator");
	
	public void testSimple() throws ArooaConversionException, IOException {
		
		Console results = new Console();
		
		Oddjob.CONSOLE.addListener(results, 
				LogLevel.DEBUG, 0, -1);
		
		OutputStream output = System.out;
		
		// Note that depending on order of test different class loader
		// could be capturing console.
		assertEquals(LoggingPrintStream.class.getName(), 
				output.getClass().getName());
		
		OutputStream test = new StdoutType().toValue();
		
		test.write(("Hello World." + EOL).getBytes());
		
		test.close();
		
		Oddjob.CONSOLE.removeListener(results);
		
		logger.debug("**********************");
		logger.debug(results.out.toString());
		logger.debug("**********************");
		
		assertEquals("Hello World." + EOL, results.out.toString());
	}
	
	public void testStdoutInOddjob() throws ArooaPropertyException, ArooaConversionException {
		
		String xml =
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <copy>" +
			"     <input>" +
			"      <identify id='hello'>" +
			"       <value>" +
			"        <buffer>Hello" + EOL + "</buffer>" +
			"       </value>" + 
			"      </identify>" + 
			"     </input>" + 
			"     <output>" +
			"      <stdout/>" +
			"     </output>" +
			"    </copy>" +
			"    <copy>" +
			"     <input>" +
			"      <buffer>World" + EOL + "</buffer>" + 
			"     </input>" + 
			"     <output>" +
			"      <stdout/>" +
			"     </output>" +
			"    </copy>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		Console results = new Console();
		
		Oddjob.CONSOLE.addListener(results, 
				LogLevel.DEBUG, 0, -1);
		
		oddjob.run();
		
		Oddjob.CONSOLE.removeListener(results);
		
		String sanityCheck = new OddjobLookup(oddjob).lookup("hello", String.class);
		assertEquals("Hello", sanityCheck.trim());
		
		oddjob.destroy();
		
		logger.debug("**********************");
		logger.debug(results.out.toString());
		logger.debug("**********************");
		
		assertTrue(results.out.toString().contains("Hello"));
		assertTrue(results.out.toString().contains("World"));		
	}
	
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
		
		Console results = new Console();
		
		Oddjob.CONSOLE.addListener(results, 
				LogLevel.DEBUG, 0, -1);
		
		oddjob.run();
		
		Oddjob.CONSOLE.removeListener(results);
		
		oddjob.destroy();
		
		assertTrue(results.out.toString().contains("Hello"));
		assertTrue(results.out.toString().contains("World"));		

		System.err.println("Where's this?");
	}
}
