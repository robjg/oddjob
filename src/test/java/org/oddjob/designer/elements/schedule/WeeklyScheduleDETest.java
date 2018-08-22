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
import org.oddjob.schedules.schedules.WeeklySchedule;
import org.oddjob.schedules.units.DayOfWeek;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class WeeklyScheduleDETest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(WeeklyScheduleDETest.class);
	
   @Before
   public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<schedules:weekly xmlns:schedules='http://rgordon.co.uk/oddjob/schedules'" +
				" from='Tuesday' " +
				" to='Thursday'/>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.VALUE);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(DayOfWeekScheduleDesign.class, design.getClass());
		
		WeeklySchedule test = (WeeklySchedule) OddjobTestHelper.createValueFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals(DayOfWeek.Days.TUESDAY, test.getFrom());
		assertEquals(DayOfWeek.Days.THURSDAY, test.getTo());
	}

	public static void main(String args[]) throws ArooaParseException {

		WeeklyScheduleDETest test = new WeeklyScheduleDETest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
