package org.oddjob.script;

import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Provide an {@link Evaluatable} for a {@link CompiledScript}.
 * 
 * @author rob
 *
 */
public class PreCompiled implements Evaluatable {

	private final ScriptEngine engine;
	
	private final CompiledScript compiled;
	
	public PreCompiled(ScriptEngine engine, CompiledScript compiled) {
		this.engine = engine;
		this.compiled = compiled;
	}
	
	@Override
	public Object eval() throws ScriptException {
		return compiled.eval();
	}
	
	@Override
	public Object get(String key) {
		return engine.get(key);
	}
	
	@Override
	public void put(String key, Object value) {
		engine.put(key, value);
	}
}
