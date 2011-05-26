package org.oddjob.sql;

import java.text.ParseException;

import junit.framework.TestCase;

import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.values.types.DateType;

public class SQLConversionsTest extends TestCase {

	public void testDateConversions() 
	throws ArooaParseException, NoConversionAvailableException, 
	ConversionFailedException, ParseException {
		
		ArooaSession session = new OddjobSessionFactory().createSession();
		
		ArooaConverter converter = session.getTools().getArooaConverter();
		
		DateType date = new DateType();
		date.setDate("2009-12-25");
		date.setFormat("yyyy-MM-dd");
		
		long expected = date.toDate().getTime();
		
		java.sql.Date sqlDate = converter.convert(date, java.sql.Date.class);
		java.sql.Time time = converter.convert(date, java.sql.Time.class);
		java.sql.Timestamp timestamp = converter.convert(date, java.sql.Timestamp.class);
		
		assertEquals(expected, sqlDate.getTime());
		assertEquals(expected, time.getTime());
		assertEquals(expected, timestamp.getTime());
	}
}
