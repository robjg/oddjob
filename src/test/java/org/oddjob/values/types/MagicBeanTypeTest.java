package org.oddjob.values.types;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class MagicBeanTypeTest extends OjTestCase {

   @Test
	public void testMagicBeanExample() throws ArooaParseException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/types/MagicBeanTypeExample.xml", 
				getClass().getClassLoader()));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals("We have 24 Apple(s).", lookup.lookup("e.text"));
		
		oddjob.destroy();
	}
}
