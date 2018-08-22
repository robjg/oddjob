package org.oddjob.beanbus.destinations;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import org.oddjob.OjTestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class OnlyFilterTest extends OjTestCase {

   @Test
	public void testFiltersNoStop() {
	
		List<String> results = new ArrayList<String>();
		
		OnlyFilter<String> test = new OnlyFilter<String>();
		test.setOnly(2);
		
		test.setTo(results);
		
		test.add("Apple");
		test.add("Orange");
		test.add("Pear");
		
		assertEquals(2, results.size());
	}
	
   @Test
	public void testExample() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/beanbus/destinations/OnlyFilterExample.xml", 
				getClass().getClassLoader()));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		List<?> results = lookup.lookup(
				"results.beans", List.class);
				
		assertEquals(2, results.size());
		
		Object beanBus = lookup.lookup("bean-bus");
		((Resetable) beanBus).hardReset();
		((Runnable) beanBus).run();
		
		int count = lookup.lookup(
				"results.count", int.class);
		
		assertEquals(2, count);
		
		oddjob.destroy();
	}
}
