package org.oddjob.oddballs;

import org.oddjob.arooa.ArooaDescriptor;

/**
 * Abstract representation of an Oddball.
 * 
 * @author rob
 *
 */
public interface Oddball {

	/**
	 * The ClasLoader created to load classes in the
	 * Oddball.
	 * <p>
	 * This isn't currently used by anything but it
	 * seems like a nice to have.
	 * 
	 * @return
	 */
	public ClassLoader getClassLoader();

	/**
	 * The {@link ArooaDescriptor}.
	 * 
	 * @return The ArooaDescripor. Must not be null.
	 */
	public ArooaDescriptor getArooaDescriptor();
}
