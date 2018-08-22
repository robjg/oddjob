package org.oddjob.util;


/**
 * An AutoCloseable that doeesn't throw a checked exception.
 * 
 * @author rob
 *
 */
@FunctionalInterface
public interface Restore extends AutoCloseable {

	@Override
	void close();	

	/**
	 * Provide a no-op Restore.
	 * 
	 * @return The no-op restore.
	 */
	static Restore nothing() {
		return () -> {};
	}
}
