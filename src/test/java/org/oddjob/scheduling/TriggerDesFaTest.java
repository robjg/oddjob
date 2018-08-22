/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.scheduling;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.StateConditions;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class TriggerDesFaTest extends OjTestCase {
	
	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException {
		
		String xml =  
			"<scheduling:trigger xmlns:scheduling='http://rgordon.co.uk/oddjob/scheduling'" +  
			"                  name='A Test'" +
			"				   id='this'" +
			"                  on='${this}'" +
			"                  state='EXCEPTION'" +
			"                  cancelWhen='FINISHED'" +
			"                  newOnly='true'" +
			"                  >" +
			"   <job>" +
			"    <echo>Do Something Useful</echo>" +
			"   </job>" +
			"</scheduling:trigger>";
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		Trigger trigger = (Trigger) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals(trigger, trigger.getOn());
		assertEquals(StateConditions.EXCEPTION, trigger.getState());
		assertEquals(StateConditions.FINISHED, trigger.getCancelWhen());
		assertEquals(true, trigger.isNewOnly());
		assertEquals(1, OddjobTestHelper.getChildren(trigger).length);
	}

	public static void main(String args[]) throws ArooaParseException {

		TriggerDesFaTest test = new TriggerDesFaTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
	}
}
