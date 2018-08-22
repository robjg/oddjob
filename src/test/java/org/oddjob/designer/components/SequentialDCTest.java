/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;
import org.junit.Before;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.oddjob.state.WorstStateOp;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class SequentialDCTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(SequentialDCTest.class);
	
   @Before
   public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<sequential name='Test' independent='true'" +
				"            stateOperator='WORST' transient='true'>" +
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
		
		SequentialJob test = (SequentialJob) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertEquals(true, test.isIndependent());
		assertEquals(WorstStateOp.class, test.getStateOperator().getClass());
		assertEquals(true, test.isTransient());
		
		Object[] children = OddjobTestHelper.getChildren(test);

		assertEquals(2, children.length);
	}

	public static void main(String args[]) throws ArooaParseException {

		SequentialDCTest test = new SequentialDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
