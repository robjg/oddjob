package org.oddjob.beanbus.destinations;

import javax.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.deploy.annotations.ArooaText;
import org.oddjob.arooa.life.Configured;
import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.Transformer;

/**
 * @oddjob.description Provide a Script as a {@link Filter}.
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
	
	private Transformer<F, T> transformer;
	
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
        
        transformer = invocable.getInterface(Transformer.class);
        
        if (transformer == null) {
        	throw new IllegalStateException(
        			"The script does not implement the Transformer interface.");
        }
	}
	
	@Override
	protected T filter(F from) {
		
		return transformer.transform(from);
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
