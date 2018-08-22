package org.oddjob.values;

/**
 * Consumes values set via the value property. Implementations will 
 * generally be a service that does something with each value as
 * they are set.
 * 
 * @author rob
 *
 * @param <T> The type of the value to consume.
 */
public interface ValueConsumer<T> {

	/**
	 * Accept a value.
	 * 
	 * @param value The value. This should not be null.
	 */
	public void setValue(T value);
}
