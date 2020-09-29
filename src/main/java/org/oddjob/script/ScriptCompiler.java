/*
 * Copyright  2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.oddjob.script;

import org.oddjob.util.OddjobWrapperException;

import javax.script.*;
import java.io.Reader;
import java.io.StringReader;

/**
 * This class is used to run Compile scripts.
 *
 */
public class ScriptCompiler {

    /** Script language */
    private final String language;

    private Invocable invocable;
    
    private final ClassLoader classLoader;
    
    /**
     * Constructor.
     * 
     * @param language The language. Default to JavaScript.
     */
    public ScriptCompiler(String language, ClassLoader classLoader) {
        if (language == null) {
            this.language = "JavaScript";
        }
        else {
        	this.language = language;
        }
        this.classLoader = classLoader;
	}

	public Evaluatable compileScript(String script) {
        return compileScript(new StringReader(script));
    }

    /**
     * Do the work.
     */
    public Evaluatable compileScript(Reader reader) {

        try {
            ScriptEngineManager manager = new ScriptEngineManager(
            		classLoader);

            ScriptEngine engine = manager.getEngineByName(language);

            if (engine instanceof Invocable) {
            	invocable = (Invocable) engine;
            }
            
            if (engine instanceof Compilable) {
            	CompiledScript compiled = ((Compilable) engine).compile(
            			reader);
            	return new PreCompiled(compiled);
            }
            else {
            	return new NotPreCompiled(engine, reader);
            }
        } catch (ScriptException be) {
            throw new OddjobWrapperException(be);
        }
    }

    /**
     * The Invocable that result from the last compilation if the 
     * engine supports it. Null otherwise.
     * 
     * @return An Invocable or null.
     */
    public Invocable getInvocable() {
    	return invocable;
    }
}
