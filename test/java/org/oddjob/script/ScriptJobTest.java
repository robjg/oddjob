/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.script;

import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.values.VariablesJob;

/**
 * 
 */
public class ScriptJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(ScriptJobTest.class);
	
	public void testHelloWorld() {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/ScriptHelloWorld.xml", 
				getClass().getClassLoader()));
		oj.run();
		
		assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(oj));
	}

	public void testVariableFromAndToJava() throws ArooaPropertyException, ArooaConversionException {
				
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/VariablesFromAndToOddjob.xml", 
				getClass().getClassLoader()));
		oj.run();
		
		assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(oj));
		
		String snack = new OddjobLookup(oj).lookup("e.text",
				String.class);
		
		assertEquals("apple", snack);
	}

	public void testSettingOutput() {
		
		String xml = 
			"<oddjob>" +
			" <job>" + 
			"  <script id='s' language='JavaScript'>" +
			"   <input>" +
			"    <buffer>" +
// works in Groovy, not JavaScript.
//			"results.fruit = \"apple\"" +
			"results.put('fruit', \"apple\");" +
			"    </buffer>" +
			"   </input>" +
			"   <beans>" +
			"    <bean key='results' class='java.util.HashMap'/>" +
			"   </beans>" +
			"  </script>" +
			" </job>" + 
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
		
		ScriptJob sc = (ScriptJob) new OddjobLookup(oj).lookup("s");
		Map<?, ?> results = 
			(Map<?, ?>) sc.getBeans("results");
		
		assertEquals("apple", results.get("fruit"));
	}

	public void testResult() {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/ScriptResult.xml",
				getClass().getClassLoader()));
		oj.run();
		
		assertEquals(ParentState.INCOMPLETE, oj.lastStateEvent().getState());
	}
	
	public void testSettingVariables() throws Exception {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/ScriptSettingProperty.xml", 
				getClass().getClassLoader()));
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		VariablesJob v = (VariablesJob) new OddjobLookup(oj).lookup("v");
		
		
		Object result = new DefaultConverter().convert(
				v.get("today"), Object.class);
		
		assertNotNull(result);
		assertEquals(Date.class, result.getClass());

		Object formatted = v.get("formattedToday");
		assertNotNull(formatted);
		logger.info(formatted);
	}
		
}
