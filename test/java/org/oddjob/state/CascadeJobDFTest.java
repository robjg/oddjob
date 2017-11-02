/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.state;
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
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class CascadeJobDFTest extends OjTestCase {
	private static final Logger logger = Logger.getLogger(CascadeJobDFTest.class);
	
   @Before
   public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<cascade name='Test' cascadeOn='FINISHED'" +
				"         haltOn='EXCEPTION'>" +
				" <jobs>" +
				"  <echo/>" +
				"  <echo/>" +
				" </jobs>" +
				"</cascade>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(CascadeJobDesign.class, design.getClass());
		
		CascadeJob test = (CascadeJob) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		
		Object[] children = OddjobTestHelper.getChildren(test);

		assertEquals(2, children.length);
	}

	public static void main(String args[]) throws ArooaParseException {

		CascadeJobDFTest test = new CascadeJobDFTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
