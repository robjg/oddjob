package org.oddjob.script;

/**
 * Something that can invoke something.
 * 
 * @author rob
 *
 */
public interface Invoker {

	/**
	 * Invoke something.
	 * 
	 * @param name The name of the thing to be invoked.
	 * @param parameters Parameters. never null.
	 * 
	 * @return The result of doing the invoking.
	 */
	public Object invoke(String name, InvokerArguments parameters)
	throws Throwable;
}
