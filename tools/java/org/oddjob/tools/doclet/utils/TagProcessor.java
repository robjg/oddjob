package org.oddjob.tools.doclet.utils;

import com.sun.javadoc.Tag;

/**
 * Something that can process a {@link Tag}.
 * 
 * @author rob
 *
 */
public interface TagProcessor {

	/**
	 * Process the tag. Generally the text returned will be HTML
	 * that can be inserted into the Oddjob JavaDoc or the reference
	 * manual.
	 * 
	 * @param tag The tag to process.
	 * @return Text or null if this processor can't process the given
	 * tag.
	 */
	public String process(Tag tag);
}
