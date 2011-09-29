package org.oddjob.jobs;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.oddjob.io.BufferType;
import org.xml.sax.SAXException;

public class XSLTJobTest extends XMLTestCase {

	String EOL = System.getProperty("line.separator");
	
	public void testIdentity() throws SAXException, IOException {
	
		String xml = 
			"<oddjob>" + EOL + 
			"  <job>" + EOL +
			"    <echo text='Hello'/>" + EOL +
			"  </job>" + EOL +
			"</oddjob>" + EOL;
		
		BufferType result = new BufferType();
		result.configured();
		
		XSLTJob test = new XSLTJob();
		test.setStylesheet(getClass().getResourceAsStream("styles.xsl"));
		test.setInput(new ByteArrayInputStream(xml.getBytes()));
		test.setOutput(result.toOutputStream());
		
		test.run();
				
		assertXMLEqual(xml, result.getText());
	}
	
	
	public void testParmeter() throws SAXException, IOException {
		
		String xml = 
			"<oddjob>" + EOL + 
			"  <job>" + EOL +
			"    <echo text='Hello'/>" + EOL +
			"  </job>" + EOL +
			"</oddjob>" + EOL;
		
		BufferType result = new BufferType();
		result.configured();
		
		XSLTJob test = new XSLTJob();
		test.setStylesheet(getClass().getResourceAsStream(
				"styles-with-param.xsl"));
		test.setInput(new ByteArrayInputStream(xml.getBytes()));
		test.setOutput(result.toOutputStream());
		test.setParameters("text", "Hello");
		
		test.run();
				
		assertXMLEqual(xml, result.getText());
	}
}
