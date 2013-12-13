package org.oddjob.designer.elements.schedule;

import junit.framework.TestCase;

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
import org.oddjob.schedules.schedules.WeeklySchedule;
import org.oddjob.schedules.units.DayOfWeek;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class WeeklyScheduleDETest extends TestCase {
	private static final Logger logger = Logger.getLogger(WeeklyScheduleDETest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
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
