/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;


/**
 * Something that can create {@link ServerInterfaceManager}s.
 *
 * @see ServerInterfaceHandlerFactory
 *
 * @author rob
 *
 */
public interface ServerInterfaceManagerFactory {
	
	/**
	 * Create a {@link ServerInterfaceManager} for the given object.
	 * 
	 * @param target The object that needs a manager.
	 * 
	 * @param serverSideToolkit The toolkit used for creation of the manager.
	 * 
	 * @return A ServerInterfaceManager. Never null.
	 */
	ServerInterfaceManager create(
			Object target, ServerSideToolkit serverSideToolkit);
}
