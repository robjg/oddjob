package org.oddjob.script;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.persist.MapPersister;
import org.oddjob.persist.OddjobPersister;
import org.oddjob.state.ParentState;

public class InvokeJobTest extends TestCase {

	public void testMethodExample() throws ArooaPropertyException, ArooaConversionException {
		
		OddjobPersister persister = new MapPersister();
		
		Oddjob oddjob1 = new Oddjob();
		oddjob1.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/InvokeJobMethod.xml", 
				getClass().getClassLoader()));
		oddjob1.setPersister(persister);
		
		oddjob1.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob1.lastStateEvent().getState());
		
		OddjobLookup lookup1 = new OddjobLookup(oddjob1);
				
		String result1 = lookup1.lookup("echo.text", String.class);
		
		assertEquals("Hello", result1);
		
		oddjob1.destroy();
		
		Oddjob oddjob2 = new Oddjob();
		oddjob2.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/InvokeJobMethod.xml", 
				getClass().getClassLoader()));
		oddjob2.setPersister(persister);
		
		oddjob2.load();
		
		assertEquals(ParentState.READY, 
				oddjob2.lastStateEvent().getState());
		
		OddjobLookup lookup2 = new OddjobLookup(oddjob2);
				
		String result2 = lookup2.lookup("invoke-job.result", String.class);
		
		assertEquals("Hello", result2);
		
		oddjob2.destroy();
	}
}
