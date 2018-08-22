package org.oddjob.schedules;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.oddjob.tools.ConsoleCapture;

public class ScheduleTypeTest extends OjTestCase {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleTypeTest.class);
	
   @Test
	public void testConversion() {
		
		ArooaSession session = new OddjobSessionFactory().createSession();
		
		ArooaConverter converter = session.getTools().getArooaConverter();
		
		ConversionPath<ScheduleType, String> path = converter.findConversion(ScheduleType.class, String.class);
		
		assertEquals("ScheduleType-Date-String", path.toString());
	}	
   
   @Test
    public void testNowExample() throws Exception {
    	        
        Oddjob oddjob = new Oddjob();
        oddjob.setExport("date", new ArooaObject(
        		DateHelper.parseDateTime("2009-07-25 12:15")));
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/schedules/ScheduleTypeExample.xml", 
				getClass().getClassLoader()));
        
        oddjob.run();
        
        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
        
        OddjobLookup lookup = new OddjobLookup(oddjob);
        
        // check as object
        Object now = lookup.lookup("time.now", Object.class);
        
        assertEquals(new SimpleInterval(
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

   @Test
    public void testWithTimeZone() throws Exception {
        
        Oddjob oddjob = new Oddjob();
        oddjob.setExport("date", new ArooaObject(
        		DateHelper.parseDateTime("2009-07-25 12:15")));
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/schedules/ScheduleTypeWithTimeZone.xml", 
				getClass().getClassLoader()));
        
        oddjob.run();
        
        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
        
        OddjobLookup lookup = new OddjobLookup(oddjob);
        
        String typeToText = lookup.lookup("time.now", String.class);
        
        Date date = DateHelper.parseDate("2009-07-26", "Asia/Hong_Kong");
        
        assertEquals(DateHelper.formatDateTime(date), typeToText);
        
        String echoText = lookup.lookup("echo-time.text", String.class);
        
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        assertEquals("Tomorrow in Hong Kong starts at " + 
        		sdf.format(date) + " our time.", echoText);
        
        oddjob.destroy();
    }
    
   @Test
    public void testNextBusinessDateExample() throws Exception {
        
        Oddjob oddjob = new Oddjob();
        oddjob.setExport("date", new ArooaObject(
        		DateHelper.parseDateTime("2011-12-23 12:15")));
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/schedules/ScheduleTypeNextBusinessDay.xml", 
				getClass().getClassLoader()));
        
        oddjob.run();
        
        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
        
        OddjobLookup lookup = new OddjobLookup(oddjob);
        
        String echoText = lookup.lookup("echo-time.text", String.class);
        
        assertEquals("The next business date is 2011-12-28 00:00:00.000", 
        		echoText);
        
        oddjob.destroy();
    }

   @Test
    public void testScheduleTypeForEach() throws Exception {
        
        Oddjob oddjob = new Oddjob();
        oddjob.setExport("date", new ArooaObject(
        		DateHelper.parseDateTime("2011-09-14 12:15")));
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/schedules/ScheduleTypeForEach.xml", 
				getClass().getClassLoader()));
        
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.captureConsole()) {
			
	        oddjob.run();
		}
        
        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
        
        console.dump(logger);
        
        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
        
        String[] lines = console.getLines();
        
        assertEquals(5, lines.length);
        
        assertEquals("Next due: 2011-09-20 10:30:00 up to 2011-09-21 00:00:00", 
        		lines[4].trim());
        
        oddjob.destroy();
    }
}
