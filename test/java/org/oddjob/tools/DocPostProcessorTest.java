package org.oddjob.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.custommonkey.xmlunit.XMLTestCase;
import org.oddjob.OurDirs;
import org.oddjob.io.BufferType;
import org.oddjob.util.IO;
import org.xml.sax.SAXException;

public class DocPostProcessorTest extends XMLTestCase {

	String EOL = System.getProperty("line.separator");
	
	public void testJavaFilePattern() {
		
		Pattern test = new DocPostProcessor().new JavaCodeInjector().pattern;
		
		Matcher matcher = test.matcher("bla bla {@oddjob.java.file " +
				"test/java/org/oddjob/tools/SomeJavaCode.java}--foo");
		assertTrue(matcher.find());
		
		assertEquals("test/java/org/oddjob/tools/SomeJavaCode.java", 
				matcher.group(1));
	}
	
	public void testxMLResourcePattern() {
		
		Pattern test = new DocPostProcessor.XMLResourceInjector().pattern;
		
		Matcher matcher = test.matcher("bla bla {@oddjob.xml.resource " +
				"org/oddjob/tools/SomeXML.xml}--foo");
		assertTrue(matcher.find());
		
		assertEquals("org/oddjob/tools/SomeXML.xml", 
				matcher.group(1));
	}
	
	public void testInsertFile() throws SAXException, IOException {
		
		OurDirs dirs = new OurDirs();
		
		DocPostProcessor test = new DocPostProcessor();
		
		test.setBaseDir(dirs.base());
		
		String input = 
			"<body>" + EOL +
			"<h1>Some Java</h1>" + EOL +
			"     {@oddjob.java.file test/java/org/oddjob/tools/SomeJavaCode.java}" + EOL +
			"<h1>Some XML</h1>" + EOL +
			"     {@oddjob.xml.resource org/oddjob/tools/SomeXML.xml}" + EOL +
			"</body>" + EOL;
		
		test.setInput(new ByteArrayInputStream(input.getBytes()));
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		test.setOutput(output);
	
		test.run();
		
		BufferType buffer = new BufferType();
		buffer.configured();
		
		
		InputStream expected = new FileInputStream(
				dirs.relative("/test/tools/DocPostProcessorExpected.html"));
		
		assertNotNull(expected);
		
		IO.copy(expected, 
				buffer.toOutputStream());
		
		String result = new String(output.toByteArray());
		
		System.out.println(result);
		
		assertXMLEqual(buffer.getText(), result);

	}
}
