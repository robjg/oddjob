/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
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
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class DeleteDCTest extends TestCase {
	private static final Logger logger = Logger.getLogger(DeleteDCTest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<delete name='Test' reallyRoot='true'" +
				"        logEvery='10' force='true'" +
				"        maxErrors='2'>" +
				" <files>" +
				"  <files files='b/c/*.foo'/>" +
				" </files>" +
				"</delete>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(DeleteDesign.class, design.getClass());
		
		DynaBean test = (DynaBean) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.get("name"));
		assertEquals(new Boolean(true), test.get("force"));
		assertEquals(new Boolean(true), test.get("reallyRoot"));
		assertEquals(new Integer(10), test.get("logEvery"));
		assertEquals(new Integer(2), test.get("maxErrors"));
	}

	public static void main(String args[]) throws ArooaParseException {

		DeleteDCTest test = new DeleteDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
