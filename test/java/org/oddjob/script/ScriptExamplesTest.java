package org.oddjob.script;

import java.util.Properties;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

import junit.framework.TestCase;

public class ScriptExamplesTest extends TestCase {

	public void testInvokeScriptFunction() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/InvokeScriptFunction.xml", 
				getClass().getClassLoader()));
		
		oddjob.run();

		assertEquals(JobState.COMPLETE, 
				oddjob.lastJobStateEvent().getJobState());
		
		Properties props = new OddjobLookup(oddjob).lookup(
				"properties.properties", Properties.class);

		assertEquals("Apples", props.getProperty("text.after"));
	}
	
}
