package org.oddjob.designer.elements.schedule;
import org.junit.Before;

import org.junit.Test;

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
import org.oddjob.schedules.schedules.CountSchedule;
import org.oddjob.schedules.schedules.DailySchedule;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class DailyScheduleDETest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(DailyScheduleDETest.class);
	
   @Before
   public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<schedules:daily xmlns:schedules='http://rgordon.co.uk/oddjob/schedules'" +
				"  from='11:00' to='15:00'>" +
				" <refinement>" +
				"  <schedules:count count='3'/>" +
				" </refinement>" +
				"</schedules:daily>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.VALUE);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(DailyScheduleDesign.class, design.getClass());
		
		DailySchedule test = (DailySchedule) OddjobTestHelper.createValueFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals(CountSchedule.class, test.getRefinement().getClass());
		assertEquals("11:00", test.getFrom());
		assertEquals("15:00", test.getTo());
	}
	
	public static void main(String args[]) throws ArooaParseException {

		DailyScheduleDETest test = new DailyScheduleDETest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
