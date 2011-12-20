package org.oddjob.tools.doclet.utils;

import com.sun.javadoc.Tag;

/**
 * Process in-line Tags
 * 
 * @author rob
 *
 */
public class InlineTagsProcessor implements TagsProcessor {

	@Override
	public String process(Tag[] tags) {
		StringBuilder builder = new StringBuilder();

		TagProcessor tagProcessor = new CompositeTagProcessor(
				new SeeTagProcessor(), 
				new XMLResourceTagProcessor(), 
				new FallbackTagProcessor());
		
		for (Tag tag : tags) {
			String snippet = tagProcessor.process(tag);
			if (snippet != null) {
				builder.append(snippet);				
			}
		}
		
		return builder.toString();
	}
}
