/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.elements;

import java.io.File;
import java.io.IOException;

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
import org.oddjob.io.AppendType;

/**
 *
 */
public class FileOutputDETest extends TestCase {
	private static final Logger logger = Logger.getLogger(FileOutputDETest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
	public void testCreate() throws ArooaParseException, IOException {
		
		String xml =  
				"<append>" +
				" <file>" +
				"  <file file='test/myFile.txt'/>" +
				" </file>" +
				"</append>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.VALUE);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
//		assertEquals(FileOutputDesign.class, design.getClass());
		
		AppendType test = (AppendType) Helper.createTypeFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals(new File("test/MyFile.txt").getCanonicalFile(), 
				test.getFile());
	}

	public static void main(String args[]) throws ArooaParseException, IOException {

		FileOutputDETest test = new FileOutputDETest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
