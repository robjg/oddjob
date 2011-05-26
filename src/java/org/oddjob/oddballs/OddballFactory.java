package org.oddjob.oddballs;

import java.io.File;

/**
 * Abstraction of something that can create Oddballs.
 * <p>
 * 
 * @author rob
 *
 */
public interface OddballFactory {

	/**
	 * Create an Oddball.
	 * 
	 * @param file A File that a paticular factory may or
	 * may not be able to use.
	 * @param parentLoader
	 * 
	 * @return An Oddball or null if the file can't be used by 
	 * the implementing factory.
	 */
	public Oddball createFrom(File file, ClassLoader parentLoader);
	
}
