package org.oddjob.sql;

import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class SQLResultsBeanBusTest extends TestCase {

	private static final Logger logger = Logger.getLogger(SQLResultsBeanBusTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("-----------------------------  " + getName() + "  ----------------");
	}
		
	
	public void testExample() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SQLResultsBeanBusExample.xml",
				getClass().getClassLoader()));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		List<?> results = lookup.lookup("select.results.to", List.class);
		
		assertEquals(2, results.size());
		
		oddjob.destroy();
		
	}
}
