/*
 * Copyright  2000-2004 The Apache Software Foundation
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.script.Invocable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.framework.extend.SerializableJob;
import org.oddjob.util.OddjobConfigException;

/**
 * @oddjob.description Execute a script. The script can be in any 
 * language that supports the Java Scripting Framework.
 * <p>
 * The named beans property allow values to be passed to and from the
 * script.
 * <p>
 * 
 * @oddjob.example
 * 
 * Hello World.
 * 
 * {@oddjob.xml.resource org/oddjob/script/ScriptHelloWorld.xml}
 * 
 * @oddjob.example
 * 
 * Variables from and to Oddjob.
 * 
 * {@oddjob.xml.resource org/oddjob/script/VariablesFromAndToOddjob.xml}
 * 
 * 
 * @oddjob.example
 * 
 * Using a script to set a property on a Job elsewhere in Oddjob.
 * 
 * {@oddjob.xml.resource org/oddjob/script/ScriptSettingProperty.xml}
 * 
 * @oddjob.example
 * 
 * Invoking a script to provide a substring function.
 * 
 * {@oddjob.xml.resource org/oddjob/script/InvokeScriptFunction.xml}
 * 
 * @oddjob.example
 * 
 * Setting the script job to not complete. 
 * 
 * {@oddjob.xml.resource org/oddjob/script/ScriptResult.xml}
 * 
 * @author Rob Gordon - Based on the original from Ant.
 */
public class ScriptJob extends SerializableJob {
	private static final long serialVersionUID = 2010012600;
	
	private static final Logger logger = LoggerFactory.getLogger(ScriptJob.class);
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The name of the language the script 
	 * is in.
	 * @oddjob.required No. Defaults to JavaScript.
	 */
    private transient String language;
    
    
	/** 
	 * @oddjob.property
	 * @oddjob.description A named bean which is made available to
	 * the script.
	 * @oddjob.required No.
	 */
    private transient Map<String, Object> beans;
    
	/** 
	 * @oddjob.property
	 * @oddjob.description The script provided as input from file or buffer etc.
	 * @oddjob.required Yes.
	 */
    private transient InputStream input;
    
	/** 
	 * @oddjob.property
	 * @oddjob.description The variable in the script that will be used to 
	 * provide the result. The property is designed for use with scripting
	 * languages who's execution does not produce a result. If, however 
	 * the script does produce a result and this property is set, the variable
	 * will override the scripts return value.
	 * @oddjob.required No.
	 */
    private String resultVariable;
    
	/** 
	 * @oddjob.property
	 * @oddjob.description The result of executing the script or the script
	 * variable chosen as the result variable with the {@code resultVariable}
	 * property.
	 */
    private Object result;
    
    /**
	 * @oddjob.property
	 * @oddjob.description If true then use the result to determine the 
	 * completion state of the job. If the result is not a number this 
	 * property will have no affect. 
	 * If the result is a number and 0 the job will COMPLETE, any
	 * other value and the job will be INCOMPLETE.
	 * @oddjob.required No, defaults to false.
     */
    private boolean resultForState;
    
	/** 
	 * @oddjob.property
	 * @oddjob.description Allow a scripted function to be evaluated 
	 * from elsewhere in Oddjob.
	 */
    private transient Invocable invocable;

    /** The thing that was compiles and/or run. */
    private transient Evaluatable evaluatable;
    
	/** 
	 * @oddjob.property
	 * @oddjob.description ClassLoader to load the Script Engine.
	 * @oddjob.required No. Automatically set to the current Oddjob class loader.
	 */
    private transient ClassLoader classLoader;
    
    /*
     *  (non-Javadoc)
     * @see org.oddjob.framework.SimpleJob#execute()
     */
    protected int execute() throws IOException {
    	ScriptCompiler compiler = new ScriptCompiler(language,
    			classLoader);
        
        if (input == null) {
        	throw new OddjobConfigException("No script provided!");
        }

        evaluatable = compiler.compileScript(
        		new InputStreamReader(input));
        
        logger.info("Script compiled.");
        
        invocable = compiler.getInvocable();
        
        ScriptRunner runner = new ScriptRunner(resultVariable);
        if (beans != null) {
        	runner.addBeans(beans);
        }
        
        result = runner.executeScript(evaluatable);
        
        logger.info("Script executed. Result " + result);
        
        if (resultForState) {
            if (this.result instanceof Number) {
            	return ((Number) this.result).intValue();
            }        	
        }
                
        return 0;
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
	 * Get the language.
	 *  
	 * @return The language.
	 */
	public String getLanguage() {
		return language;
	}
	
    /**
     * Get the named bean.
     * 
     * @param name The name of the bean
     * @return The bean or null if it doesn't exist.
     */
	public Object getBeans(String name) {
		if (beans == null) {
			return null;
		}
		return beans.get(name);
	}

	/**
	 * Add a named bean.
	 * 
	 * @param name The name of the bean.
	 * @param value The bean.
	 */
	public void setBeans(String name, Object value) {
		if (beans == null) {
			beans = new HashMap<String, Object>();
		}
		logger().debug("Adding bean (" + name
				+ ", [" + value + "]");
		beans.put(name, value);
	}	
		
	/**
	 * Get the input.
	 * 
	 * @return The input.
	 */
	public InputStream getInput() {
		return input;
	}
	
	/**
	 * Set the input.
	 * 
	 * @param input The input.
	 */
	public void setInput(InputStream input) {
		this.input = input;
	}	

	public Invocable getInvocable() {
		return invocable;
	}

	/** 
	 * @oddjob.property variables
	 * @oddjob.description Provide access to variables declared within the
	 * script.
	 */
	public Object getVariables(String key) {
		return evaluatable.get(key);
	}

	public String getResultVariable() {
		return resultVariable;
	}

	public void setResultVariable(String resultVariable) {
		this.resultVariable = resultVariable;
	}

	public boolean isResultForState() {
		return resultForState;
	}

	public void setResultForState(boolean resultForState) {
		this.resultForState = resultForState;
	}

	public Object getResult() {
		return result;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Inject
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
}
