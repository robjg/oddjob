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
import org.oddjob.jobs.ExecJob;

/**
 *
 */
public class ExecDCTest extends TestCase {
	private static final Logger logger = Logger.getLogger(ExecDCTest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<exec name='Test'>" +
				"do stuff" +
				" <environment>" +
				"  <value key='SNACK' value='apple'/>" +
				" </environment>" +
				" <stdout>" +
				"  <buffer/>" +
				" </stdout>" +
				" <stderr>" +
				"  <buffer/>" +
				" </stderr>" +
				" <stdin>" +
				"  <buffer/>" +
				" </stdin>" +
				"</exec>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(ExecDesign.class, design.getClass());
		
		ExecJob test = (ExecJob) Helper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertEquals("apple", test.getEnvironment("SNACK"));
		assertNotNull(test.getStderr());
		assertNotNull(test.getStdout());
		assertNotNull(test.getStdin());
	}

	public static void main(String args[]) throws ArooaParseException {

		ExecDCTest test = new ExecDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
