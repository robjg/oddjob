package org.oddjob.script;

import org.oddjob.arooa.convert.ArooaConversionException;

/**
 * Arguments for passing to an {@link Invoker}.
 * 
 * @author rob
 *
 */
public interface InvokerArguments {

	/**
	 * The number of arguments.
	 * 
	 * @return The number, 0 or greater.
	 */
	public int size();
	
	/**
	 * Get the argument.
	 * 
	 * @param index The 0 based index of the argument.
	 * @param type The type the argument should be.
	 * 
	 * @return An argument of the correct type. May be null.
	 * 
	 * @throws ArooaConversionException If an argument of the correct
	 * type can not be provided.
	 */
	public <T> T getArgument(int index, Class<T> type)
	throws ArooaConversionException;
}
