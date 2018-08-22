package org.oddjob.script;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Something that can be evaluated. This is currently only a further 
 * abstraction of compiled and not compiled {@link ScriptEngine}s.
 * 
 * @author rob
 *
 */
public interface Evaluatable {

	/**
	 * Evaluate the evaluatable.
	 * 
	 * @return The object returned by the script or null if the script
	 * does not return a value.
	 * 
	 * @throws ScriptException
	 */
	public Object eval() throws ScriptException;
	
	/**
	 * Get a bean from the engines bindings.
	 * 
	 * @param key The scripts variable name.
	 * @return The bean if one of the name exists.
	 */
	public Object get(String key);
	
	/**
	 * Add a bean
	 * @param key
	 * @param value
	 */
	public void put(String key, Object value);
}
