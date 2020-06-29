/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

/**
 * Something that when transferred across the network can be referred to by a local proxy.
 */
public interface Exportable {

	/**
	 * Provide the transportable representation of this thing.
	 *
	 * @return A transportable representation. Never null.
	 */
	Transportable exportTransportable();
	
}
