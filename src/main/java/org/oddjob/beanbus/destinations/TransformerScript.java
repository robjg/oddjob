package org.oddjob.beanbus.destinations;

import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.deploy.annotations.ArooaText;
import org.oddjob.arooa.life.Configured;
import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusFilter;

import javax.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.function.Function;

/**
 * @oddjob.description Provide a Script as an {@link BusFilter}.
 * 
 * @oddjob.example
 * 
 * A simple example.
 * 
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/TransformerScriptExample.xml}
 * 
 * @author rob
 *
 * @param <F> The from type.
 * @param <T> The to type.
 */
public class TransformerScript<F, T> extends AbstractFilter<F, T> {

	private String script;
	
	private Function<F, T> function;
	
	private ClassLoader classLoader;
	
	private String language;
	
	@SuppressWarnings("unchecked")
	@Configured
	public void configured() throws ScriptException {
		
		if (language == null) {
			language = "JavaScript";
		}
		
        ScriptEngineManager manager = new ScriptEngineManager(
        		classLoader);

        ScriptEngine engine = manager.getEngineByName(language);

        engine.eval(script);
        
        if (! (engine instanceof Invocable)) {
        	throw new IllegalStateException(
        			"Script Engine is not Invocable.");
        }
        
        Invocable invocable = (Invocable) engine;
        
        function = invocable.getInterface(Function.class);
        
        if (function == null) {
        	throw new IllegalStateException(
        			"The script does not implement the Function interface.");
        }
	}
	
	@Override
	protected T filter(F from) {
		
		return function.apply(from);
	}

	public String getScript() {
		return script;
	}

	@ArooaText
	public void setScript(String script) {
		this.script = script;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@ArooaHidden
	@Inject
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
}
