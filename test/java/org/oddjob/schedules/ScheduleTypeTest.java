package org.oddjob.schedules;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConsoleCapture;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionPath;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class ScheduleTypeTest extends TestCase {

	private static final Logger logger = Logger.getLogger(ScheduleTypeTest.class);
	
	public void testConversion() {
		
		ArooaSession session = new OddjobSessionFactory().createSession();
		
		ArooaConverter converter = session.getTools().getArooaConverter();
		
		ConversionPath<ScheduleType, String> path = converter.findConversion(ScheduleType.class, String.class);
		
		assertEquals("ScheduleType-Date-String", path.toString());
	}	
   
    public void testInOddjob() throws Exception {
    	        
        Oddjob oddjob = new Oddjob();
        oddjob.setExport("date", new ArooaObject(
        		DateHelper.parseDateTime("2009-07-25 12:15")));
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/schedules/ScheduleTypeExample.xml", 
				getClass().getClassLoader()));
        
        oddjob.run();
        
        OddjobLookup lookup = new OddjobLookup(oddjob);
        
        // check as object
        Object now = lookup.lookup("time.now", Object.class);
        
        assertEquals(IntervalTo.class, now.getClass());

        assertEquals(new IntervalTo(
        		DateHelper.parseDateTime("2009-07-25 12:15")), now);
        
        // Text
        String typeToText = lookup.lookup("time.now", String.class);
        assertEquals("2009-07-25 12:15:00.000", typeToText);
        
        // check as date that is formatted.
        String timeFormatted = lookup.lookup("time.formatted", String.class);
        
        assertEquals("12:15 PM", timeFormatted);
        
        String echoText = lookup.lookup("echo-time.text", String.class);
        
        assertEquals("The time now is " + timeFormatted, echoText);
        
        oddjob.destroy();
    }

    public void testScheduleTypeForEach() throws Exception {
        
        Oddjob oddjob = new Oddjob();
        oddjob.setExport("date", new ArooaObject(
        		DateHelper.parseDateTime("2011-09-14 12:15")));
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/schedules/ScheduleTypeForEach.xml", 
				getClass().getClassLoader()));
        
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
        oddjob.run();
        
        console.close();
        
        console.dump(logger);
        
        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
        
        String[] lines = console.getLines();
        
        assertEquals(5, lines.length);
        
        assertEquals("2011-09-20 10:30:00 up to 2011-09-21 00:00:00", 
        		lines[4].trim());
        
        oddjob.destroy();
    }
}
