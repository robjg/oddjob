package org.oddjob.script;

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

/**
 *
 */
public class InvokeEitherDFTest extends TestCase {
	private static final Logger logger = Logger.getLogger(InvokeEitherDFTest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
	public void testCreateJob() throws ArooaParseException {
		
		String xml =  
				"<invoke name='Test' function='static concat'>" +
				" <source>" +
				"  <class name='" + InvokeEitherDFTest.class.getName() + "'/>" +
				" </source>" +
				" <parameters>" +
				"  <tokenizer text='a, b, c'/>" +
				" </parameters>" +
				"</invoke>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(InvokeJobDesign.class, design.getClass());
		
		InvokeJob test = (InvokeJob) Helper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		
		test.run();
		
		assertEquals("abc", test.getResult());
	}

	public void testCreateType() throws Throwable {
		
		String xml =  
				"<invoke function='static concat'>" +
				" <source>" +
				"  <class name='" + InvokeEitherDFTest.class.getName() + "'/>" +
				" </source>" +
				" <parameters>" +
				"  <tokenizer text='a, b, c'/>" +
				" </parameters>" +
				"</invoke>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.VALUE);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(InvokeTypeDesign.class, design.getClass());
		
		InvokeType test = (InvokeType) Helper.createTypeFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		Object result = test.toValue();
		
		assertEquals("abc", result);
	}
	
	public static String concat(String... strings) {
		StringBuilder result = new StringBuilder();
		for (String string : strings) {
			result.append(string);
		}
		return result.toString();
	}
	
	public static void main(String args[]) throws Throwable {

		InvokeEitherDFTest test = new InvokeEitherDFTest();
//		test.testCreateJob();
		test.testCreateType();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
