/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;
import org.junit.Before;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.structural.RepeatJob;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class RepeatDCTest extends OjTestCase {
	private static final Logger logger = Logger.getLogger(RepeatDCTest.class);
	
   @Before
   public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<repeat name='Test' times='3'>" +
				" <job>" +
				"  <echo/>" +
				" </job>" +
				" <values>" +
				"  <sequence from='1' to='3'/>" +
				" </values>" +
				" <until>" +
				"  <value value='true'/>" +
				" </until>" +
				"</repeat>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(RepeatDesign.class, design.getClass());
		
		RepeatJob test = (RepeatJob) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertEquals(true, test.isUntil());
		assertEquals(3, test.getTimes());
		
		Object[] children = OddjobTestHelper.getChildren(test);

		assertEquals(1, children.length);
	}

	public static void main(String args[]) throws ArooaParseException {

		RepeatDCTest test = new RepeatDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
