package org.oddjob.tools.doclet.utils;

import org.oddjob.doclet.CustomTagNames;
import org.oddjob.tools.includes.PlainTextResourceLoader;

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
				new GenericIncludeTagProcessor(CustomTagNames.TEXT_RESOURCE_TAG, 
						new PlainTextResourceLoader()),
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
