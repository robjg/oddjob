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
import org.oddjob.jobs.job.StopJob;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class JustJobDCTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(JustJobDCTest.class);
	
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
	public void testStop() throws ArooaParseException {
		
		String xml =  
				"<stop id='test' name='Test' job='${test}'/>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(JustJobDesign.class, design.getClass());
		
		StopJob test = (StopJob) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertEquals(test, test.getJob());

	}
	
	public static void main(String args[]) throws ArooaParseException {

		JustJobDCTest test = new JustJobDCTest();
		test.testStop();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
