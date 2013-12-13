package org.oddjob.logging.log4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OurDirs;

public class LogoutTypeTest extends TestCase {

	private static final Logger logger = Logger.getLogger(LogoutTypeTest.class);
	
	@Override
	protected void setUp() throws Exception {
		logger.debug("-------------------  " + getName() + "  --------------");
	}
	
	
	private class Results extends AppenderSkeleton {
		
		List<Object> messages = new ArrayList<Object>();
		
		@Override
		protected void append(LoggingEvent arg0) {
			messages.add(arg0.getMessage());
		}

		@Override
		public void close() {
		}

		@Override
		public boolean requiresLayout() {
			return false;
		}
	}
	
	String EOL = System.getProperty("line.separator");
	
	String logName = "org.oddjob.wow.Test";
	
	public void testSimple() throws ArooaConversionException, IOException {
		
		Results results = new Results();
		
		Logger.getLogger(logName).addAppender(results);
		
		LogoutType logout = new LogoutType();		
		logout.setLogger(logName);
		
		OutputStream test = logout.toValue();
		
		test.write(("Hello World." + EOL).getBytes());
		
		test.close();
		
		Logger.getLogger(logName).removeAppender(results);
		
		assertEquals(1, results.messages.size());
		assertEquals("Hello World.", results.messages.get(0));
	}
	
	public void testLogoutInOddjob() throws ArooaPropertyException, ArooaConversionException {
		
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
			"      <logout/>" +
			"     </output>" +
			"    </copy>" +
			"    <copy>" +
			"     <input>" +
			"      <buffer>World" + EOL + "</buffer>" + 
			"     </input>" + 
			"     <output>" +
			"      <logout logger='" + logName + "'/>" +
			"     </output>" +
			"    </copy>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		Results results = new Results();
		
		Logger logger = Logger.getLogger(logName);;
		logger.addAppender(results);
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());

		logger.removeAppender(results);
		
		String sanityCheck = new OddjobLookup(oddjob).lookup("hello", String.class);
		assertEquals("Hello", sanityCheck.trim());
		
		oddjob.destroy();
		
		assertTrue(results.messages.get(0).toString().contains("World"));		
	}
	
	public void testExample() {
		
		OurDirs dirs = new OurDirs();
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/logging/log4j/LogoutExample.xml", 
				getClass().getClassLoader()));
		oddjob.setArgs(new String[] { dirs.base().toString() } );
		
		Results results = new Results();
		
		Logger logger = Logger.getLogger(LogoutType.class);;
		logger.addAppender(results);
		
		oddjob.run();
				
		assertEquals(ParentState.COMPLETE,
				oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
		
		assertTrue(results.messages.get(0).toString().trim().equals("Test"));		
	}
	
}
