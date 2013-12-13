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
import org.oddjob.schedules.schedules.BrokenSchedule;
import org.oddjob.schedules.schedules.DateSchedule;
import org.oddjob.schedules.schedules.IntervalSchedule;
import org.oddjob.schedules.schedules.NowSchedule;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class BrokenScheduleDETest extends TestCase {
	private static final Logger logger = Logger.getLogger(BrokenScheduleDETest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<schedules:broken xmlns:schedules='http://rgordon.co.uk/oddjob/schedules'>" +
				" <schedule>" +
				"  <schedules:interval interval='00:15:00'/>" +
				" </schedule>" +
				" <breaks>" +
				"  <schedules:date on='2011-09-16'/>" +
				" </breaks>" +
				" <alternative>" +
				"  <schedules:now/>" +
				" </alternative>" +
				"</schedules:broken>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.VALUE);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(BrokenScheduleDesign.class, design.getClass());
		
		BrokenSchedule test = (BrokenSchedule) OddjobTestHelper.createValueFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals(IntervalSchedule.class, test.getSchedule().getClass());
		assertEquals(DateSchedule.class, test.getBreaks().getClass());
		assertEquals(NowSchedule.class, test.getAlternative().getClass());
	}
	
	public static void main(String args[]) throws ArooaParseException {

		BrokenScheduleDETest test = new BrokenScheduleDETest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
