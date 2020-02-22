/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.io;
import org.junit.Before;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

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
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.io.AppendType;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class AppendTypeDFTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(AppendTypeDFTest.class);
	
   @Before
   public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException, IOException {
		
		String xml =  
				"<append file='test/MyFile.txt'/>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.VALUE);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(FileTypeDesign.class, design.getClass());
		
		AppendType test = (AppendType) OddjobTestHelper.createValueFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals(new File("test/MyFile.txt"), test.getFile());
	}

	public static void main(String args[]) throws ArooaParseException, IOException {

		AppendTypeDFTest test = new AppendTypeDFTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
