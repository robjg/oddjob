/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.values.types;

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
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;

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
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/types/FormatTypeExample.xml", 
				getClass().getClassLoader()));
		
		oj.run();
		
		String result = new OddjobLookup(oj).lookup("file-check.file", 
				String.class);
		
		assertEquals("Data-20051225-000123.dat", result);
	}
}
