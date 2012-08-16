/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import java.util.Map;

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
import org.oddjob.jmx.JMXServiceJob;

/**
 *
 */
public class JMXServiceDCTest extends TestCase {
	private static final Logger logger = Logger.getLogger(JMXServiceDCTest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<jmx:service xmlns:jmx='http://rgordon.co.uk/oddjob/jmx' " +
				"  name='Test'" +
				"  connection='localhost:2012'>" +
				"  <environment>" +
				"   <jmx:client-credentials username='username' password='password'/>" +
				"  </environment>" +
				"</jmx:service>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(JMXServiceDesign.class, design.getClass());
		
		JMXServiceJob test = (JMXServiceJob) Helper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertEquals("localhost:2012", test.getConnection());
		
		Map<String, ?> env = test.getEnvironment();
		String[] credentials = (String[]) env.get("jmx.remote.credentials");
		
		assertEquals("username", credentials[0]);
		assertEquals("password", credentials[1]);
	}

	public static void main(String args[]) throws ArooaParseException {

		JMXServiceDCTest test = new JMXServiceDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
