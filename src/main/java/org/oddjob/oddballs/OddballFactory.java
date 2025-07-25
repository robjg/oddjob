package org.oddjob.oddballs;

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
	 * @param parentLoader The parent class loader.
	 * 
	 * @return An Oddball or null if the file can't be used by 
	 * the implementing factory.
	 */
	Oddball createFrom(ClassLoader parentLoader) throws Exception;
	
}
