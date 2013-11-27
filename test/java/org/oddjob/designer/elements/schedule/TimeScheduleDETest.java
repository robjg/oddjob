package org.oddjob.designer.elements.schedule;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.OddjobTestHelper;
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
import org.oddjob.schedules.schedules.TimeSchedule;

/**
 *
 */
public class TimeScheduleDETest extends TestCase {
	private static final Logger logger = Logger.getLogger(TimeScheduleDETest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<schedules:time xmlns:schedules='http://rgordon.co.uk/oddjob/schedules'" +
				"  from='11:00' toLast='15:00'>" +
				" <refinement>" +
				"  <schedules:count count='3'/>" +
				" </refinement>" +
				"</schedules:time>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.VALUE);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(TimeScheduleDesign.class, design.getClass());
		
		TimeSchedule test = (TimeSchedule) OddjobTestHelper.createValueFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals(CountSchedule.class, test.getRefinement().getClass());
		assertEquals("11:00", test.getFrom());
		assertEquals("15:00", test.getToLast());
	}
	
	public static void main(String args[]) throws ArooaParseException {

		TimeScheduleDETest test = new TimeScheduleDETest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
