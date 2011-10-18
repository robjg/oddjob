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
import org.oddjob.jobs.structural.SequentialJob;

/**
 *
 */
public class SequentialDCTest extends TestCase {
	private static final Logger logger = Logger.getLogger(SequentialDCTest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<sequential name='Test' independent='true'>" +
				" <jobs>" +
				"  <echo/>" +
				"  <echo/>" +
				" </jobs>" +
				"</sequential>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(SequentialDesign.class, design.getClass());
		
		SequentialJob test = (SequentialJob) Helper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertEquals(true, test.isIndependent());
		
		Object[] children = Helper.getChildren(test);

		assertEquals(2, children.length);
	}

	public static void main(String args[]) throws ArooaParseException {

		SequentialDCTest test = new SequentialDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
