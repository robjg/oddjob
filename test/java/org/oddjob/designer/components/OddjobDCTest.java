/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OddjobInheritance;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.OurDirs;

/**
 *
 */
public class OddjobDCTest extends OjTestCase {
	private static final Logger logger = Logger.getLogger(OddjobDCTest.class);
	
	@Rule public TestName name = new TestName();

	public String getName() {
        return name.getMethodName();
    }

	@Before
    public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException {
		
		OurDirs dirs = new OurDirs();
		
		File testFile = dirs.relative("work/myjobs.xml");
		
		String xml = 
				"<oddjob name='Test' file='" + testFile.getPath() + 
						"' inheritance='SHARED'>" +
				" <properties>" +
				"  <properties>" +
				"   <values>" +
				"    <value key='favourite.fruit' value='apple'/>" +
				"   </values>" +
				"  </properties>" +
				" </properties>" +
				" <classLoader>" +
				"  <url-class-loader>" +
				"   <files>" +
				"    <file file='mystuff.jar'/>" +
				"   </files>" +
				"  </url-class-loader>" +
				" </classLoader>" +
				"</oddjob>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();		
		
		assertEquals(OddjobDesign.class, design.getClass());
		
		Oddjob test = (Oddjob) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertEquals(testFile, test.getFile());
		assertEquals(OddjobInheritance.SHARED, test.getInheritance());
	}

	public static void main(String args[]) throws ArooaParseException {

		OddjobDCTest test = new OddjobDCTest();
		test.testCreate();

		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
	}

}
