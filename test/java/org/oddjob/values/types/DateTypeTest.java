/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.values.types;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConverterHelper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.ManualClock;

/**
 * 
 */
public class DateTypeTest extends TestCase {
	private static final Logger logger = Logger.getLogger(DateTypeTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("----------------  " + getName() + "  -------------");
	}
	
	// check we get date and string
	public void testConversions() throws Exception {
		DateType dt = new DateType();

		dt.setDate("2005-12-25");
		
		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		Date date = converter.convert(dt, Date.class);
		assertEquals("date", new SimpleDateFormat("dd-MMM-yy").parse("25-DEC-05"), date);
		
		Calendar calendar = converter.convert(dt, Calendar.class);
		Calendar expectedCal = Calendar.getInstance();
		expectedCal.clear();
		expectedCal.set(2005, 11, 25);
		assertEquals("calendar", expectedCal, calendar);
		
		String string = converter.convert(dt, String.class);
		assertEquals("date", DateHelper.formatDateTime(
				DateHelper.parseDate("2005-12-25")), string);		
	}
	
	public void testTimeZone() throws Exception {
		DateType dt = new DateType();

		dt.setDate("2005-12-25");
		dt.setTimeZone("US/Hawaii");
		
		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		Calendar calendar = converter.convert(dt, Calendar.class);
		Calendar expectedCal = Calendar.getInstance(TimeZone.getTimeZone("US/Hawaii"));
		expectedCal.clear();
		expectedCal.set(2005, 11, 25);
		assertEquals(expectedCal.getTime(), calendar.getTime());
		
	}
	
	public void testInOddjob() throws ArooaConversionException, ParseException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <variables id='v'>" +
			"   <myDate>" +
			"    <date date='2005-12-25'/>" +
			"   </myDate>" +
			"   <myDate2>" +
			"    <date date='2005-12-25' format='yyyy-MM-dd' timeZone='US/Hawaii'/>" +
			"   </myDate2>" +
			"  </variables>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
		
		Date result = new OddjobLookup(oj).lookup("v.myDate", Date.class);
		
		assertEquals(DateHelper.parseDate("2005-12-25"), result);
		
		Date result2 = new OddjobLookup(oj).lookup("v.myDate2", Date.class);
		
		assertEquals(DateHelper.parseDate("2005-12-25", 
						TimeZone.getTimeZone("US/Hawaii")), result2);
	}
	
	public void testInvalidTimeZone() throws ParseException {
		
		DateType test = new DateType();
		test.setDate("2009");
		test.setFormat("yyyy");
		test.setTimeZone("My Front Room");
		
		Date result = test.toDate();
		
		assertEquals(DateHelper.parseDate("2009-01-01", "GMT"), result);		
	}
	
	public void testSimpleDateExample() throws ArooaParseException, ParseException {
		
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/values/types/SimpleDateExample.xml", 
    			getClass().getClassLoader()));
    	
    	DateType date = (DateType)	parser.getRoot();
    	
    	Date expected = 
    			DateHelper.parseDateTime("2009-12-25 12:30");
    	
    	assertEquals(expected, date.toDate());
	}
	
	public void testFormatExample() throws ArooaParseException, ParseException {
		
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/values/types/DateFormatExample.xml", 
    			getClass().getClassLoader()));
    	
    	DateType date = (DateType)	parser.getRoot();
    	
    	Date expected = 
    			DateHelper.parseDateTime("2009-12-25 12:30");
    	
    	assertEquals(expected, date.toDate());
	}
	
	public void testDateTimezoneExample() throws ParseException {
				
		logger.debug(TimeZone.getDefault());
		
		Oddjob oj = new Oddjob(); 
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/types/DateWithTimeZoneExample.xml", 
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.captureConsole();
				
		oj.run();
		
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		Date there = DateHelper.parseDateTime("2009-12-25", "US/Hawaii");
		
		assertEquals("Christmas in Hawaii starts at " +
				DateHelper.formatDateTime(there)+ ".", 
				lines[0].trim());
		
		assertEquals(1, lines.length);
		
		oj.destroy();
		
	}
	
	public void testDateShortcuts() throws ParseException {

		DateType test = new DateType();
		test.setClock(new ManualClock("2013-01-16 07:30"));
		
		test.setDate("TODAY");
		
		assertEquals(DateHelper.parseDateTime("2013-01-16 00:00"), 
				test.toDate());
		
		test.setDate("NOW");
		
		assertEquals(DateHelper.parseDateTime("2013-01-16 07:30"), 
				test.toDate());
		
		test.setDate("TOMORROW");
		
		assertEquals(DateHelper.parseDateTime("2013-01-17 00:00"), 
				test.toDate());
		
		test.setDate("YESTERDAY");
		
		assertEquals(DateHelper.parseDateTime("2013-01-15 00:00"), 
				test.toDate());
	}
	
	public void testDateShortcutsExample() throws ParseException {
		
		Oddjob oddjob = new Oddjob(); 
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/types/DateShortcutsExample.xml", 
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.captureConsole();
				
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		console.close();
		console.dump(logger);
				
		oddjob.destroy();
		
	}
}
