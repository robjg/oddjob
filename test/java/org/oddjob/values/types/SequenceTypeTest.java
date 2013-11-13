package org.oddjob.values.types;

import java.util.Iterator;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class SequenceTypeTest extends TestCase {

	public void testExample() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/types/SequenceExample.xml",
				getClass().getClassLoader()));

		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Iterable<?> iterable = lookup.lookup("vars.ourSequence", Iterable.class);
		
		Iterator<?> it = iterable.iterator();
		
		assertTrue(it.hasNext());
		assertEquals(2, it.next());
		
		assertTrue(it.hasNext());
		assertEquals(4, it.next());
		
		assertTrue(it.hasNext());
		assertEquals(6, it.next());
		
		assertTrue(it.hasNext());
		assertEquals(8, it.next());

		assertFalse(it.hasNext());
		
		oddjob.destroy();
	}
	
}
