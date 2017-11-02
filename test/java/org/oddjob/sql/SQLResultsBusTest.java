package org.oddjob.sql;
import org.junit.Before;

import org.junit.Test;

import java.util.List;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class SQLResultsBusTest extends OjTestCase {

	private static final Logger logger = Logger.getLogger(SQLResultsBusTest.class);
	
    @Before
    public void setUp() throws Exception {

		
		logger.info("-----------------------------  " + getName() + "  ----------------");
	}
		
	
   @Test
	public void testExample() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SQLResultsBusExample.xml",
				getClass().getClassLoader()));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		List<?> results = lookup.lookup("select.results", List.class);
		
		assertEquals(2, results.size());
		
		oddjob.destroy();
		
	}
	
   @Test
	public void testExample2() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SQLResultsBusExample2.xml",
				getClass().getClassLoader()));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		List<?> results = lookup.lookup("bean-capture.beans", List.class);
		
		assertEquals(3, results.size());
		
		oddjob.destroy();
		
	}
}
