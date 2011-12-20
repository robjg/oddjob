package org.oddjob.tools.doclet.utils;

import com.sun.javadoc.Tag;

/**
 * A {@link TagProcessor} that tries a number of different processors
 * until it finds one that works.
 * 
 * @author rob
 *
 */
public class CompositeTagProcessor implements TagProcessor {

	private final TagProcessor[] processors;
	
	/**
	 * Constructor.
	 * 
	 * @param processors Processors to try.
	 */
	public CompositeTagProcessor(TagProcessor... processors) {
		this.processors = processors;
	}
	
	@Override
	public String process(Tag tag) {
		for (TagProcessor processor : processors) {
			String snipet = processor.process(tag);
			if (snipet != null) {
				return snipet;
			}
		}
		return null;
	}
}
