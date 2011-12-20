package org.oddjob.tools.doclet.utils;

import com.sun.javadoc.Tag;

/**
 * Something that can process a number of tags.
 * 
 * @author rob
 *
 */
public interface TagsProcessor {

	/**
	 * Process the tags. Generally the text returned will be HTML
	 * that can be inserted into the Oddjob JavaDoc or the reference
	 * manual.
	 * 
	 * @param tags The tags to process.
	 * @return The text. This is not expected to be null.
	 */
	public String process(Tag[] tags);
}
