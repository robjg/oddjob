package org.oddjob.designer.elements.schedule;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Helper;
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
import org.oddjob.schedules.schedules.IntervalSchedule;

/**
 *
 */
public class CountScheduleDETest extends TestCase {
	private static final Logger logger = Logger.getLogger(CountScheduleDETest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<schedules:count xmlns:schedules='http://rgordon.co.uk/oddjob/schedules'" +
				"  count='5' identifier='apples'>" +
				" <refinement>" +
				"  <schedules:interval interval='00:15:00'/>" +
				" </refinement>" +
				"</schedules:count>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.VALUE);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(CountScheduleDesign.class, design.getClass());
		
		CountSchedule test = (CountSchedule) Helper.createTypeFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals(IntervalSchedule.class, test.getRefinement().getClass());
		assertEquals(5, test.getCount());
		assertEquals("apples", test.getIdentifier());
	}
	
	public static void main(String args[]) throws ArooaParseException {

		CountScheduleDETest test = new CountScheduleDETest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}