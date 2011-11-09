/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.script;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Helper;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;

/**
 *
 */
public class ScriptDCTest extends TestCase {
	private static final Logger logger = Logger.getLogger(ScriptDCTest.class);
	
	DesignInstance design;
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
			"<script name='Test Script' language='JavaScript'" +
			"  resultVariable='result' resultForState='true'>" +
			" <input>" +
			"  <buffer>" +
			"println(\"Hello\");" +
			"  </buffer>" +
			" </input>" +
			" <beans>" +
			"  <value key='fruit' value='apple'/>" +
			" </beans>" +
			"</script>";
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(ScriptDesign.class, design.getClass());
		
		ScriptJob test = (ScriptJob) Helper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("JavaScript", test.getLanguage());
		assertEquals("apple", test.getBeans("fruit"));
		assertEquals("result", test.getResultVariable());
		assertEquals(true, test.isResultForState());
	}
	
	public static void main(String args[]) throws ArooaParseException {

		ScriptDCTest test = new ScriptDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}
}
