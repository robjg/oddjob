/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.script;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.OddjobTestHelper;
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
public class ScriptDFTest extends TestCase {
	private static final Logger logger = Logger.getLogger(ScriptDFTest.class);
	
	DesignInstance design;
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
			"<script id='this' name='Test Script' language='JavaScript'" +
			"  resultVariable='result' resultForState='true'>" +
			" <input>" +
			"  <buffer>" +
			"println(\"Hello\");" +
			"  </buffer>" +
			" </input>" +
			" <beans>" +
			"  <value key='fruit' value='apple'/>" +
			" </beans>" +
			" <classLoader>" +
			"  <value value='${this.class.classLoader}'/>" +
			" </classLoader>" +
			"</script>";
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(ScriptDesign.class, design.getClass());
		
		ScriptJob test = (ScriptJob) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("JavaScript", test.getLanguage());
		assertEquals("apple", test.getBeans("fruit"));
		assertEquals("result", test.getResultVariable());
		assertEquals(ScriptJob.class.getClassLoader(), test.getClassLoader());
		assertEquals(true, test.isResultForState());
	}
	
	public static void main(String args[]) throws ArooaParseException {

		ScriptDFTest test = new ScriptDFTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}
}
