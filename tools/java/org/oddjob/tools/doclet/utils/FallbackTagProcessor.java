package org.oddjob.tools.doclet.utils;

import com.sun.javadoc.Tag;

/**
 * A {@link TagProcessor} that just returns the text of the tag.
 * 
 * @author rob
 *
 */
public class FallbackTagProcessor implements TagProcessor {

	@Override
	public String process(Tag tag) {
		return tag.text();
	}
}
