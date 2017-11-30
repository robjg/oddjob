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
import org.oddjob.schedules.schedules.YearlySchedule;
import org.oddjob.schedules.units.Month;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class YearlyScheduleDETest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(YearlyScheduleDETest.class);
	
   @Before
   public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<schedules:yearly xmlns:schedules='http://rgordon.co.uk/oddjob/schedules'" +
				" fromMonth='January' " +
				" toMonth='March'/>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.VALUE);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(YearlyScheduleDesign.class, design.getClass());
		
		YearlySchedule test = (YearlySchedule) OddjobTestHelper.createValueFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals(Month.Months.JANUARY, test.getFromMonth());
		assertEquals(Month.Months.MARCH, test.getToMonth());
	}

	public static void main(String args[]) throws ArooaParseException {

		YearlyScheduleDETest test = new YearlyScheduleDETest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
