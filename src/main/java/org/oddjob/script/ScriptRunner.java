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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.script.ScriptException;

/**
 * This class is used to run scripts
 *
 */
public class ScriptRunner {

    private final String resultVariable;
    
    /** Beans to be provided to the script */
    private Map<String, Object> beans = new HashMap<String, Object>();

    /**
     * Constructor
     */
    public ScriptRunner(String resultVariable) {
    	this.resultVariable = resultVariable;
	}
    
    /**
     * Add a list of named objects to the list to be exported to the script
     *
     * @param dictionary a map of objects to be placed into the script context
     *        indexed by String names.
     */
    public void addBeans(Map<String, Object> dictionary) {
        for (Iterator<String> i = dictionary.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            
                Object val = dictionary.get(key);
                addBean(key, val);
        }
    }

    /**
     * Add a single object into the script context.
     *
     * @param key the name in the context this object is to stored under.
     * @param bean the object to be stored in the script context.
     */
    public void addBean(String key, Object bean) {
        boolean isValid = key.length() > 0
            && Character.isJavaIdentifierStart(key.charAt(0));

        for (int i = 1; isValid && i < key.length(); i++) {
            isValid = Character.isJavaIdentifierPart(key.charAt(i));
        }

        if (isValid) {
            beans.put(key, bean);
        }
    }

    /**
     * Do the work of running the script.
     *
     * @return The result of running the script.
     */
    public Object executeScript(Evaluatable evaluatable) {

        try {
            for (Iterator<String> i = beans.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                Object value = beans.get(key);
                evaluatable.put(key, value);
            }

            Object result = evaluatable.eval();
            
            if (resultVariable != null) {
            	return evaluatable.get(resultVariable);
            }
            else {
            	return result;
            }
            
        } catch (ScriptException be) {
            throw new RuntimeException(be);
        }
    }

}
