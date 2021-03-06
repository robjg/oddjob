/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.apache.commons.beanutils.DynaBean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OjTestCase;
import org.oddjob.OurDirs;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.tools.OddjobTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class EchoDCTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(EchoDCTest.class);
	
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
	public void testCreate() throws ArooaParseException, IOException {
		
		File testFile = OurDirs.workPathDir(getClass().getSimpleName(), true)
				.resolve("test.txt").toFile();
		
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
		
		DynaBean test = (DynaBean) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.get("name"));
		assertEquals("Hello", test.get("text"));
	}

	public static void main(String args[]) throws ArooaParseException, IOException {

		EchoDCTest test = new EchoDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
