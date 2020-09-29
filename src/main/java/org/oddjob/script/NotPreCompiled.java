package org.oddjob.script;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.Reader;
import java.util.Objects;

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
		this.engine = Objects.requireNonNull(engine);
		this.reader = Objects.requireNonNull(reader);
	}

	@Override
	public ScriptContext getScriptContext() {
		return engine.getContext();
	}

	@Override
	public Object eval(ScriptContext scriptContext) throws ScriptException {
		return engine.eval(reader, scriptContext);
	}
}
