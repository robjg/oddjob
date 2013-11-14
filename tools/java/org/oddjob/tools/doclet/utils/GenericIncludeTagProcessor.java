package org.oddjob.tools.doclet.utils;

import org.oddjob.tools.includes.IncludeLoader;

import com.sun.javadoc.Tag;

/**
 * Process Oddjob XML resource tag.
 * 
 * @author rob
 *
 */
public class GenericIncludeTagProcessor implements TagProcessor {

	private final String tag;
	
	private final IncludeLoader loader;
	
	public GenericIncludeTagProcessor(String tag, IncludeLoader loader) {
		this.tag = tag;
		this.loader  = loader;
	}
	
	@Override
	public String process(Tag tag) {
		
		if (! tag.name().equals(this.tag)) {
			return null;
		}
		
		String path = tag.text();
		
		return loader.load(path);		
	}
}
