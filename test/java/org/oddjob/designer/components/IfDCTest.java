/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

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
import org.oddjob.state.IfJob;

/**
 *
 */
public class IfDCTest extends TestCase {
	private static final Logger logger = Logger.getLogger(IfDCTest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<state:if xmlns:state='http://rgordon.co.uk/oddjob/state' " +
				" state='!INCOMPLETE'" +
				" name='Test'>" +
				" <jobs>" +
				"  <echo>Condition</echo>" +
				"  <echo>Then</echo>" +
				"  <echo>Else</echo>" +
				" </jobs>" +
				"</state:if>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(IfDesign.class, design.getClass());
		
		IfJob test = (IfJob) Helper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertNotNull("Hello", test.getState());

		Object[] children = Helper.getChildren(test);
		
		assertEquals(3, children.length);
	}

	public static void main(String args[]) throws ArooaParseException {

		IfDCTest test = new IfDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
