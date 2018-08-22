package org.oddjob.schedules.units;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.NoConversionAvailableException;

public class DayOfWeekTest extends OjTestCase {

   @Test
	public void testBadString() throws NoConversionAvailableException, ConversionFailedException {
		
		ArooaSession session = new OddjobSessionFactory().createSession();
		
		ArooaConverter converter = session.getTools().getArooaConverter();
	
		try {
			converter.convert("Fooday", DayOfWeek.class);
			fail("Should fail");
		}
		catch (ConversionFailedException e) {
			
			Throwable cause = e.getCause();
			assertEquals("[Fooday] is not a valid day of week. Valid values are [MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY] or an integer 1 to 7.", 
					cause.getMessage());
			
		}
	}
}
