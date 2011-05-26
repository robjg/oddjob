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

import java.io.Reader;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This class is used to run BSF scripts
 *
 */
public class ScriptCompiler {

    /** Script language */
    private String language;

    private Invocable invocable;
    
    /**
     * Do the work.
     *
     * @exception BuildException if someting goes wrong exectuing the script.
     */
    public Evaluatable compileScript(Reader reader) {
        if (language == null) {
            throw new RuntimeException("script language must be specified");
        }

        try {
            ScriptEngineManager manager = new ScriptEngineManager ();

            ScriptEngine engine = manager.getEngineByName(language);

            if (engine instanceof Invocable) {
            	invocable = (Invocable) engine;
            }
            
            if (engine instanceof Compilable) {
            	CompiledScript compiled = ((Compilable) engine).compile(
            			reader);
            	return new PreCompiled(engine, compiled);
            }
            else {
            	return new NotPreCompiled(engine, reader);
            }
        } catch (ScriptException be) {
            throw new RuntimeException(be);
        }
    }

    /**
     * Defines the language (required).
     *
     * @param language the scripting language name for the script.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Get the script language
     *
     * @return the script language
     */
    public String getLanguage() {
        return language;
    }
    
    public Invocable getInvocable() {
    	return invocable;
    }
}
