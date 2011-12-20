package org.oddjob.tools.taglet;

import org.oddjob.tools.doclet.utils.InlineTagsProcessor;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

/**
 * Base class for Taglet functionality for an Oddjob block tag.
 * 
 * @author rob
 *
 */
abstract public class BaseBlockTaglet implements Taglet {	

	@Override
	final public boolean inConstructor() {
		return false;
	}

	@Override
	final public boolean inOverview() {
		return false;
	}

	@Override
	final public boolean inPackage() {
		return false;
	}

	@Override
	final public boolean isInlineTag() {
		return false;
	}

	@Override
	public String toString(Tag tag) {
		String text = new InlineTagsProcessor().process(tag.inlineTags());
		if (tag.holder() instanceof ClassDoc) {
			return "<h4>" + getTitle() + "</h4>" + text;
		}
		else {
			return "<p><b>" + getTitle() + "</b>: " + text;
		}
	}

	@Override
	public String toString(Tag[] tags) {
		StringBuilder builder = new StringBuilder();
		for (Tag tag : tags) {
			builder.append(toString(tag));
			
		}
		return builder.toString();
	}
	
	/**
	 * Used as the title of the Taglet.
	 * 
	 * @return The title. Must not be null.
	 */
	abstract public String getTitle();
}
