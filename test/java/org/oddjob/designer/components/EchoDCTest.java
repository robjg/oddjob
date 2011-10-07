/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Helper;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OurDirs;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.EchoJob;

/**
 *
 */
public class EchoDCTest extends TestCase {
	private static final Logger logger = Logger.getLogger(EchoDCTest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
	public void testCreate() throws ArooaParseException {
		
		OurDirs dirs = new OurDirs();
		
		File testFile = dirs.relative("work/test.txt");
		
		String xml =  
				"<echo name='Test' ><![CDATA[Hello]]>" +
				" <output>" +
				"  <file file='" + testFile.getPath() + "'/>" +
				" </output>" +
				"</echo>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(EchoDesign.class, design.getClass());
		
		EchoJob test = (EchoJob) Helper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertEquals("Hello", test.getText());
	}

	public static void main(String args[]) throws ArooaParseException {

		EchoDCTest test = new EchoDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
