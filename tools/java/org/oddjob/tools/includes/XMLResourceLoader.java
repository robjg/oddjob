package org.oddjob.tools.includes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.oddjob.doclet.CustomTagNames;
import org.oddjob.io.ResourceType;
import org.oddjob.jobs.XSLTJob;

/**
 * Creates XML that can be inserted into JavaDoc or another XML document from
 * an XML class path resource.
 * 
 * The style-sheet used is courtesy of: http://lenzconsulting.com/xml-to-string/
 * 
 * @author rob
 *
 */
public class XMLResourceLoader implements IncludeLoader, CustomTagNames {

	@Override
	public boolean canLoad(String tag) {
		return XML_RESOURCE_TAG.equals(tag);
	}
	
	@Override
	public String load(String resource) {
		
		try {
			FilterFactory filterFactory = new FilterFactory(resource);
			
			InputStream input = new ResourceType(
					filterFactory.getResourcePath()).toInputStream();

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
