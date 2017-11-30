/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
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
import org.oddjob.state.IfJob;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class IfDCTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(IfDCTest.class);
	
	@Rule public TestName name = new TestName();

	public String getName() {
        return name.getMethodName();
    }

	@Before
    public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
   @Test
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
		
		IfJob test = (IfJob) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertNotNull("Hello", test.getState());

		Object[] children = OddjobTestHelper.getChildren(test);
		
		assertEquals(3, children.length);
	}

	public static void main(String args[]) throws ArooaParseException {

		IfDCTest test = new IfDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
