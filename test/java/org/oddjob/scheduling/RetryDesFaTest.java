/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.scheduling;

import org.junit.Test;

import org.oddjob.OjTestCase;

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
import org.oddjob.jobs.job.ResetActions;
import org.oddjob.state.StateConditions;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class RetryDesFaTest extends OjTestCase {
	
	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException {
		
		String xml =  
			"<scheduling:retry xmlns:scheduling='http://rgordon.co.uk/oddjob/scheduling'" + 
			"                  xmlns:schedules='http://rgordon.co.uk/oddjob/schedules'" + 
			"                  name='A Test'" +
			"                  timeZone='America/Chicago'" +
			"                  haltOn='FAILURE'" +
			"                  reset='SOFT'" +
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
		
		Retry test = (Retry) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("America/Chicago", test.getTimeZone());
		assertEquals(StateConditions.FAILURE, test.getHaltOn());
		assertEquals(ResetActions.SOFT, test.getReset());
		assertNotNull(test.getSchedule());
		assertEquals(1, OddjobTestHelper.getChildren(test).length);
	}

	public static void main(String args[]) throws ArooaParseException {

		RetryDesFaTest test = new RetryDesFaTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
	}
}
