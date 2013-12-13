/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.values.properties;

import junit.framework.TestCase;

import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.values.properties.PropertiesJob;

/**
 *
 */
public class PropertiesJobDesFaTest extends TestCase {
	
	DesignInstance design;
	
	public void testCreate() throws ArooaParseException {
		
		String xml =  
			"<properties name='A Test'" +
			"		     id='this'" +
			"            environment='env'" +
			"            extract='ba'" +
			"            prefix='foo'" +
			"            override='true'" +
			"            substitute='true'" +
			"            fromXML='false'" +
			"                  >" +
			"   <sets>" +
			"    <properties>" +
			"     <values>" +
			"      <value key='ba.snack.fruit' value='apple'/>" +
			"     </values>" +
			"    </properties>" +
			"   </sets>" +
			"   <values>" +
			"      <value key='ba.snack.carbs' value='cracker'/>" +
			"   </values>" +
			"</properties>";
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
    	
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(PropertiesJobDesign.class, design.getClass());
		
		PropertiesJob test = (PropertiesJob) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("env", test.getEnvironment());
		assertEquals("ba", test.getExtract());
		assertEquals("foo", test.getPrefix());
		assertEquals(true, test.isOverride());
		assertEquals(true, test.isSubstitute());
				
		test.run();
		
		assertEquals("apple", test.getProperties().get("foo.snack.fruit"));
		assertEquals("cracker", test.getProperties().get("foo.snack.carbs"));
		
	}

	public static void main(String args[]) throws ArooaParseException {

		PropertiesJobDesFaTest test = new PropertiesJobDesFaTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
	}
}
