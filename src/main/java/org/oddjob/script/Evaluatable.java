package org.oddjob.script;

import javax.script.ScriptContext;
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
	 * Get the Script Context for the Engine. For Nashorn this has to come from the Engine
	 * to get the correct Engine Bindings.
	 *
	 * @return A Script Context. Must not be null.
	 */
	ScriptContext getScriptContext();

	/**
	 * Evaluate the evaluatable.
	 *
	 * @param scriptContext Context to evaluate script in. Must not be null;
	 * @return The object returned by the script or null if the script
	 * does not return a value.
	 *
	 * @throws ScriptException
	 */
	Object eval(ScriptContext scriptContext) throws ScriptException;
	
}
