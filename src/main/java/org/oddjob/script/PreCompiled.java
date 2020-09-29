package org.oddjob.script;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.util.Objects;

/**
 * Provide an {@link Evaluatable} for a {@link CompiledScript}.
 * 
 * @author rob
 *
 */
public class PreCompiled implements Evaluatable {

	private final CompiledScript compiled;
	
	public PreCompiled(CompiledScript compiled) {
		this.compiled = Objects.requireNonNull(compiled);
	}

	@Override
	public ScriptContext getScriptContext() {
		return compiled.getEngine().getContext();
	}

	@Override
	public Object eval(ScriptContext scriptContext) throws ScriptException {
		return compiled.eval(scriptContext);
	}
}
