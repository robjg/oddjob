package org.oddjob.tools.includes;

import java.io.InputStream;

import org.oddjob.doclet.CustomTagNames;
import org.oddjob.io.ResourceType;

/**
 * Creates Plain Text that can be inserted into JavaDoc or another HTML document from
 * an file class path resource.
 * 
 * @author rob
 *
 */
public class PlainTextResourceLoader implements IncludeLoader, CustomTagNames {

	@Override
	public boolean canLoad(String tag) {
		return TEXT_RESOURCE_TAG.equals(tag);
	}
	
	@Override
	public String load(String resource) {
		
		try {
			InputStream input = new ResourceType(
					resource).toInputStream();

			return new PlainTextToHTML().toHTML(input);
		}
		catch (Exception e) {
			return "<p><em>" + e.toString() + "</em></p>" + EOL;
		}
	}
}
