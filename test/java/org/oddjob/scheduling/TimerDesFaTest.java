/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.scheduling;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.oddjob.Helper;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.deploy.BeanDescriptorHelper;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.schedules.schedules.WeeklySchedule;

/**
 *
 */
public class TimerDesFaTest extends TestCase {
	
	DesignInstance design;
	
	public void testCreate() throws ArooaParseException, URISyntaxException {
		
		String xml =  
			"<scheduling:timer xmlns:scheduling='http://rgordon.co.uk/oddjob/scheduling'" + 
			"                  xmlns:s='http://rgordon.co.uk/oddjob/schedules'" + 
			"                  name='A Schedule'" +
			"                  timeZone='America/Chicago'" +
			"                  haltOnFailure='true'" +
			"                  skipMissedRuns='true'>" +
			"   <schedule>" +
			"    <s:weekly on=\"7\">" +
			"     <refinement>" +
			"     <s:time from=\"12:00\" to=\"16:00\"/>" +
			"     </refinement>" +
			"    </s:weekly>" +
			"   </schedule>" +
			"   <job>" +
			"    <echo>Do Something Useful</echo>" +
			"   </job>" +
			"</scheduling:timer>";
		
    	ArooaDescriptor descriptor =
    		new OddjobDescriptorFactory().createDescriptor(null);
    	
    	ArooaSession session = new StandardArooaSession(descriptor);
    	    	
    	ArooaDescriptor sd = session.getArooaDescriptor();
    	
		InstantiationContext instantiationContext = 
			new InstantiationContext(ArooaType.COMPONENT, null);
		
    	ArooaClass arooaClass = sd.getElementMappings(
    			).mappingFor(new ArooaElement(
    					new URI("http://rgordon.co.uk/oddjob/scheduling"), 
    					"timer"), instantiationContext);
    	
    	assertEquals(SimpleArooaClass.class, arooaClass.getClass());
    	
    	ArooaBeanDescriptor beanDescriptor = sd.getBeanDescriptor(
    			arooaClass, session.getTools().getPropertyAccessor());
    	
    	assertEquals("job", beanDescriptor.getComponentProperty());
    	assertEquals(ArooaType.COMPONENT, new BeanDescriptorHelper(
				beanDescriptor).getArooaType("job"));
    	
		DesignParser parser = new DesignParser(session);
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		Timer timer = (Timer) Helper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("America/Chicago", timer.getTimeZone());
		assertEquals(7, ((WeeklySchedule) timer.getSchedule()).getFrom().getDayNumber());
		assertEquals(true, timer.isHaltOnFailure());
		assertEquals(true, timer.isSkipMissedRuns());
		assertEquals(1, Helper.getChildren(timer).length);
	}

	public static void main(String args[]) 
	throws ArooaParseException, URISyntaxException {

		TimerDesFaTest test = new TimerDesFaTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
	}
}
