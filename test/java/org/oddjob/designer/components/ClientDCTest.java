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
import org.oddjob.jmx.JMXClientJob;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class ClientDCTest extends OjTestCase {
	private static final Logger logger = Logger.getLogger(ClientDCTest.class);
	
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
				"<jmx:client xmlns:jmx='http://rgordon.co.uk/oddjob/jmx' " +
				"  name='Test'" +
				"  connection='localhost:2012'" +
				"  heartbeat='5000' logPollingInterval='3000'" +
				"  maxConsoleLines='200' maxLoggerLines='300'>" +
				"  <environment>" +
				"   <jmx:client-credentials username='username' password='password'/>" +
				"  </environment>" +
				"</jmx:client>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(ClientDesign.class, design.getClass());
		
		JMXClientJob test = (JMXClientJob) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertEquals("localhost:2012", test.getConnection());
		assertEquals(5000, test.getHeartbeat());
		assertEquals(3000, test.getLogPollingInterval());
		assertEquals(200, test.getMaxConsoleLines());
		assertEquals(300, test.getMaxLoggerLines());
		
		Map<String, ?> env = test.getEnvironment();
		String[] credentials = (String[]) env.get("jmx.remote.credentials");
		
		assertEquals("username", credentials[0]);
		assertEquals("password", credentials[1]);
	}

	public static void main(String args[]) throws ArooaParseException {

		ClientDCTest test = new ClientDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
