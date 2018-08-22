/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.Map;

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
import org.oddjob.jmx.JMXServiceJob;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class JMXServiceDCTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(JMXServiceDCTest.class);

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
				"<jmx:service xmlns:jmx='http://rgordon.co.uk/oddjob/jmx' " +
				"  name='Test'" +
				"  connection='localhost:2012' heartbeat='700'>" +
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
		
		JMXServiceJob test = (JMXServiceJob) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertEquals("localhost:2012", test.getConnection());
		assertEquals(700L, test.getHeartbeat());
		
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
