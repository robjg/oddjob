package org.oddjob.arooa.types;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class ImportTypeExamplesTest extends OjTestCase {
	private static final Logger logger = Logger.getLogger(
			ImportTypeExamplesTest.class);
	
   @Test
	public void testFruitExample() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/ImportExample.xml",
				getClass().getClassLoader()));

		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		String pathA = lookup.lookup("vars.pathA", String.class);
		String pathB = lookup.lookup("vars.pathB", String.class);
		
		logger.info(pathA);
		logger.info(pathB);
		
		assertEquals(pathA, pathB);
		
		oddjob.destroy();
	}
}
