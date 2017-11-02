/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.parsing.CutAndPasteSupport;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLArooaParser;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.xmlunit.matchers.CompareMatcher;

/**
 *
 */
public class VariablesDCTest extends OjTestCase {
	private static final Logger logger = Logger.getLogger(VariablesDCTest.class);
	
	DesignInstance design;
	
   @Before
   public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}
	
   @Test
	public void testCreate() throws Exception {
		
		String xml =  
			"<variables id='vars'>" +
			" <fruit>" +
			"  <value value='Apple'/>" +
			" </fruit>" +
			"</variables>";

    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
    			
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor)); 
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
				
		String paste = 
			"<veg>" +
			" <value value='Potatoes'/>" +
			"</veg>";
		
		CutAndPasteSupport.paste(design.getArooaContext(), 1, 
				new XMLConfiguration("TEST", paste));
		
		XMLArooaParser xmlParser = new XMLArooaParser();

		xmlParser.parse(design.getArooaContext().getConfigurationNode());
		
		String EOL = System.getProperty("line.separator");
		
		String expected = 			
			"<variables id=\"vars\">" + EOL +
			"    <fruit>" + EOL +
			"        <value value=\"Apple\"/>" + EOL +
			"    </fruit>" + EOL +
			"    <veg>" + EOL +
			"        <value value=\"Potatoes\"/>" + EOL +
			"    </veg>" + EOL +
			"</variables>" + EOL;

		assertThat(xmlParser.getXml(), CompareMatcher.isSimilarTo(expected));
	}
	
   @Test
	public void testParseRubbish() throws Exception {
		
		String xml =  
			"<variables id='vars'>" +
			" <fruit>" +
			"  <rubbish/>" +
			" </fruit>" +
			"</variables>";
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor)); 
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();

		XMLArooaParser xmlParser = new XMLArooaParser();

		xmlParser.parse(design.getArooaContext().getConfigurationNode());
		
		String EOL = System.getProperty("line.separator");
		
		String expected = 			
			"<variables id=\"vars\">" + EOL +
			"    <fruit>" + EOL +
			"        <rubbish/>" + EOL +
			"    </fruit>" + EOL +
			"</variables>" + EOL;

		assertThat(xmlParser.getXml(), CompareMatcher.isSimilarTo(expected));
	}
	
   @Test
	public void testParseNoValue() throws Exception {
		
		String xml =  
			"<variables id='vars'>" +
			" <fruit/>" +
			"</variables>";
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor)); 
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();

		assertEquals(VariablesDesign.class, design.getClass());
		
		XMLArooaParser xmlParser = new XMLArooaParser();

		xmlParser.parse(design.getArooaContext().getConfigurationNode());
		
		String EOL = System.getProperty("line.separator");
		
		String expected = 			
			"<variables id=\"vars\"/>" + EOL;

		assertThat(xmlParser.getXml(), CompareMatcher.isSimilarTo(expected));
	}
	
	public static void main(String args[]) throws Exception {

		VariablesDCTest test = new VariablesDCTest();
		test.testParseNoValue();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
	}
}
