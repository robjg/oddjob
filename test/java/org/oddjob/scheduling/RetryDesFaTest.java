/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.scheduling;

import junit.framework.TestCase;

import org.oddjob.Helper;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;

/**
 *
 */
public class RetryDesFaTest extends TestCase {
	
	DesignInstance design;
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
			"<scheduling:retry xmlns:scheduling='http://rgordon.co.uk/oddjob/scheduling'" + 
			"                  xmlns:schedules='http://rgordon.co.uk/oddjob/schedules'" + 
			"                  name='A Test'" +
			"                  timeZone='America/Chicago'" +
			"                  limits='${limits}'" +
			"                  >" +
			"   <schedule>" +
			"    <schedules:interval interval='00:15'/>" +
			"   </schedule>" +
			"   <job>" +
			"    <echo>Do Something Useful</echo>" +
			"   </job>" +
			"</scheduling:retry>";
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
		
    	ArooaSession session = new StandardArooaSession(descriptor);

		DesignParser parser = new DesignParser(
				session);
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(RetryDesign.class, design.getClass());
		
		Retry test = (Retry) Helper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("America/Chicago", test.getTimeZone());
		assertNotNull(test.getSchedule());
		assertEquals(1, Helper.getChildren(test).length);
	}

	public static void main(String args[]) throws ArooaParseException {

		RetryDesFaTest test = new RetryDesFaTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
	}
}
