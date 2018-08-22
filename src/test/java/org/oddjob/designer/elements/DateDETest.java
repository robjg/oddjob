/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.elements;
import org.junit.Before;

import org.junit.Test;

import java.text.ParseException;

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
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.values.types.DateType;

/**
 *
 */
public class DateDETest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(DateDETest.class);
	
   @Before
   public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException, ParseException {
		
		String xml =  
				"<date date='01-mar-2013'" +
				"      format='dd-MMM-yyyy'" +
				"      timeZone='GMT+01'>" +
				" <clock>" +
				"  <bean class='org.oddjob.tools.ManualClock'" +
				"        dateText='2013-01-16'/>" +
				" </clock>" +
				"</date>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.VALUE);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(DateDesign.class, design.getClass());
		
		DateType test = (DateType) OddjobTestHelper.createValueFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("01-mar-2013", test.getDate());
		assertEquals("dd-MMM-yyyy", test.getFormat());
		assertEquals("GMT+01", test.getTimeZone());
		assertNotNull(test.getClock());
		
		assertEquals(DateHelper.parseDate("2013-03-01", "GMT+01"),
				test.toDate());
	}

	public static void main(String args[]) throws ArooaParseException, ParseException {

		DateDETest test = new DateDETest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
