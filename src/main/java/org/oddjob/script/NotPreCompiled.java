package org.oddjob.script;

import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Provide an {@link Evaluatable} for a not compile {@link ScriptEngine}.
 * 
 * @author rob
 *
 */
public class NotPreCompiled implements Evaluatable {

	private final ScriptEngine engine;
	
	private final Reader reader;
	
	public NotPreCompiled(ScriptEngine engine, Reader reader) {
		this.engine = engine;
		this.reader = reader;
	}
	
	@Override
	public Object eval() throws ScriptException {
		return engine.eval(reader);
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
