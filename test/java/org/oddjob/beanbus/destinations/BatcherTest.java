package org.oddjob.beanbus.destinations;

import java.util.List;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class BatcherTest extends TestCase {

	
	@SuppressWarnings("unchecked")
	public void testExample() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/beanbus/destinations/BatcherExample.xml", 
				getClass().getClassLoader()));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		List<?> results = lookup.lookup(
				"results.beans", List.class);
				
		assertEquals(3, results.size());
		
		List<String> list1 = (List<String>) results.get(0);
		List<String> list2 = (List<String>) results.get(1);
		List<String> list3 = (List<String>) results.get(2);

		assertEquals("Apple", list1.get(0));
		assertEquals("Orange", list1.get(1));
		assertEquals(2, list1.size());
		assertEquals("Pear", list2.get(0));
		assertEquals("Kiwi", list2.get(1));
		assertEquals(2, list2.size());
		assertEquals("Banana", list3.get(0));
		assertEquals(1, list3.size());

		Object beanBus = lookup.lookup("bean-bus");
		((Resetable) beanBus).hardReset();
		((Runnable) beanBus).run();
		
		int batcherCount = lookup.lookup(
				"batcher.count", int.class);
		int resultsCount = lookup.lookup(
				"results.count", int.class);
		
		assertEquals(5, batcherCount);
		assertEquals(3, resultsCount);
		
		oddjob.destroy();
	}
	
}
