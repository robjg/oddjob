package org.oddjob.jobs;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.Helper;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;

public class CheckJobDesFaTest extends TestCase {

	DesignInstance design;
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
			"<check name='A Test'" +
			"		     id='this'" +
			"            value='42'" +
			"            eq='42'" +
			"            ne='2'" +
			"            lt='43'" +
			"            le='42'" +
			"            gt='41'" +
			"            ge='42'" +
			"            null='false'" +
			"            z='false'" +
			"                  >" +
			"</check>";
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
    	
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(CheckDesign.class, design.getClass());
		
		DynaBean test = (DynaBean) Helper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("A Test", test.get("name"));
		assertEquals("42", test.get("value"));
		assertEquals(new ArooaObject("42"), test.get("eq"));
		assertEquals(new ArooaObject("2"), test.get("ne"));
		assertEquals(new ArooaObject("43"), test.get("lt"));
		assertEquals(new ArooaObject("42"), test.get("le"));
		assertEquals(new ArooaObject("41"), test.get("gt"));
		assertEquals(new ArooaObject("42"), test.get("ge"));
		assertEquals(new Boolean(false), test.get("null"));
		assertEquals(new Boolean(false), test.get("z"));
		
	}

	public static void main(String args[]) throws ArooaParseException {

		CheckJobDesFaTest test = new CheckJobDesFaTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
	}
}
