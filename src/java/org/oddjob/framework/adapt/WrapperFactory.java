package org.oddjob.framework.adapt;

/**
 * Something the can create a wrapper for a component.
 * 
 * @author rob
 *
 * @param <T> The type of the component to be wrapped.
 */
public interface WrapperFactory<T> {

	/**
	 * Get the Interfaces the wrapper adds to the component.
	 * 
	 * @param wrapped The component to be wrapped.
	 * 
	 * @return
	 */
	public Class<?>[] wrappingInterfacesFor(T wrapped);
	
	/**
	 * Create a wrapper.
	 * 
	 * @param wrapped The component to be wrapped.
	 * @param proxy The dynamic proxy.
	 * 
	 * @return The Wrapper.
	 */
	public ComponentWrapper wrapperFor(T wrapped, Object proxy);
	
}
