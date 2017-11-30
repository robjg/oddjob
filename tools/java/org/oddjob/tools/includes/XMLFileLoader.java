package org.oddjob.tools.includes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.doclet.CustomTagNames;
import org.oddjob.jobs.XSLTJob;

/**
 * Creates XML that can be inserted into JavaDoc or another XML document from
 * an XML file.
 * 
 * The style-sheet used is courtesy of: http://lenzconsulting.com/xml-to-string/
 * 
 * @author rob
 *
 */
public class XMLFileLoader implements IncludeLoader, CustomTagNames {

	private static final Logger logger = LoggerFactory.getLogger(XMLFileLoader.class);
	
	private final File base;
	
	public XMLFileLoader(File base) {
		this.base = base;
	}
	
	@Override
	public boolean canLoad(String tag) {
		return XML_FILE_TAG.equals(tag);
	}
	
	@Override
	public String load(String fileName) {
		
		try {
			FilterFactory filterFactory = new FilterFactory(fileName);
						
			File file = new File(base, filterFactory.getResourcePath());
			
			logger.info("Reading file " + file);
			
			InputStream input = new FileInputStream(file);
			
			String xml = filterFactory.getTextLoader().load(input);
			
			InputStream stylesheet = 
				getClass().getResourceAsStream("xml-2-string.xsl");
			
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			
			XSLTJob transform = new XSLTJob();
			transform.setStylesheet(stylesheet);
			transform.setInput(new ByteArrayInputStream(xml.getBytes()));
			transform.setOutput(result);
			
			transform.run();
			
			return "<pre class=\"xml\">" + EOL + 
				new String(result.toByteArray()) + 
				"</pre>" + EOL;
		}
		catch (Exception e) {
			return "<p><em>" + e.toString() + "</em></p>" + EOL;
		}
	}
}
