/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.values.types;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConverterHelper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.ManualClock;
import org.oddjob.tools.OddjobTestHelper;

/**
 * 
 */
public class FormatTypeTest extends TestCase {
	private static final Logger logger = Logger.getLogger(FormatTypeTest.class);
	
	
	@Override
	protected void tearDown() throws Exception {
		TimeZone.setDefault(null);
	}
	
	public void testLocalDate() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		Date date = DateHelper.parseDateTime("2005-11-01 22:00");
		
		FormatType ft = new FormatType();
		ft.setDate(date);
		ft.setFormat("yyyyMMdd");
		
		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		String result = converter.convert(ft, String.class);
		
		assertEquals("20051101", result);
	}
	
	public void testTimeZoneDate() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		Date date = DateHelper.parseDateTime("2005-11-01 22:00", "America/Chicago");
		logger.debug(date);
		FormatType ft = new FormatType();
		ft.setDate(date);
		ft.setFormat("yyyyMMdd");
		ft.setTimeZone("America/Chicago");
		
		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		String result = converter.convert(ft, String.class);
		
		assertEquals("20051101", result);
	}
	
	public void testNumberFormat() throws Exception {
		
		FormatType ft = new FormatType();
		ft.setNumber(new Integer(22));
		ft.setFormat("##0.00");
		
		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		String result = converter.convert(ft, String.class);
		
		assertEquals("22.00", result);
	}

	public void testInOddjob() throws ArooaConversionException, ParseException {

		File file = new File(getClass().getResource(
				"FormatTypeExample.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		oddjob.run();
		
		assertEquals(ParentState.INCOMPLETE, 
				oddjob.lastStateEvent().getState());
		
		String result = new OddjobLookup(oddjob).lookup("file-check.file", 
				String.class);
		
		assertEquals("Data-20051225-000123.dat", result);
		
		oddjob.destroy();
	}
	
	public void testFormatTimeNowExample() throws ArooaConversionException, ParseException {

		ManualClock ourClock = new ManualClock("2013-01-30 08:17");
		
		File file = new File(getClass().getResource(
				"FormatTimeNow.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		oddjob.setExport("our-clock", new ArooaObject(ourClock));
		ConsoleCapture console = new ConsoleCapture();
		console.captureConsole();
				
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());

		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		String[] expected = OddjobTestHelper.streamToLines(getClass(
				).getResourceAsStream("FormatTimeNow.txt"));
		
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i], lines[i].trim());
		}
		
		assertEquals(1, lines.length);
		
		oddjob.destroy();
	}
}
