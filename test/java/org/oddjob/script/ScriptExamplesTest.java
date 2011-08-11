package org.oddjob.script;

import java.util.Properties;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class ScriptExamplesTest extends TestCase {

	public void testInvokeScriptFunction() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/InvokeScriptFunction.xml", 
				getClass().getClassLoader()));
		
		oddjob.run();

		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		Properties props = new OddjobLookup(oddjob).lookup(
				"properties.properties", Properties.class);

		assertEquals("Apples", props.getProperty("text.after"));
	}
	
}
