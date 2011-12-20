package org.oddjob.tools.doclet.utils;

import org.oddjob.tools.includes.XMLResourceLoader;

import com.sun.javadoc.Tag;

/**
 * Process Oddjob XML resource tag.
 * 
 * @author rob
 *
 */
public class XMLResourceTagProcessor implements TagProcessor {

	@Override
	public String process(Tag tag) {
		
		if (! tag.name().equals(XMLResourceLoader.XML_RESOURCE_TAG)) {
			return null;
		}
		
		String path = tag.text();
		
		return new XMLResourceLoader().load(path);		
	}
}
