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

import org.oddjob.arooa.deploy.annotations.ArooaText;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.runtime.SessionBindings;
import org.oddjob.framework.extend.SerializableJob;
import org.oddjob.io.DevNullType;
import org.oddjob.logging.ConsoleOwner;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LoggingOutputStream;
import org.oddjob.logging.cache.LogArchiveImpl;
import org.oddjob.util.OddjobConfigException;
import org.oddjob.util.OddjobWrapperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @oddjob.description Execute a script. The script can be in any
 * language that supports the Java Scripting Framework.
 * <p>
 * The named beans property allow values to be passed to and from the
 * script.
 * <p>
 * Script output is captured in a console that is visible from Oddjob Explorer
 * in addition to any output properties.
 *
 * @oddjob.example Hello World.
 * {@oddjob.xml.resource org/oddjob/script/ScriptHelloWorld.xml}
 *
 * @oddjob.example Variables from and to Oddjob.
 * {@oddjob.xml.resource org/oddjob/script/VariablesFromAndToOddjob.xml}
 *
 * @oddjob.example Using a script to set a property on a Job elsewhere in Oddjob.
 * {@oddjob.xml.resource org/oddjob/script/ScriptSettingProperty.xml}
 *
 * @oddjob.example Invoking a script to provide a substring function.
 * {@oddjob.xml.resource org/oddjob/script/InvokeScriptFunction.xml}
 *
 * @oddjob.example Setting the script job to not complete.
 * {@oddjob.xml.resource org/oddjob/script/ScriptResult.xml}
 *
 * @oddjob.example Setting the script job to not complete.
 * {@oddjob.xml.resource org/oddjob/script/ScriptResult.xml}
 *
 * @oddjob.example Defining Java Functions in JavaScript.
 * {@oddjob.xml.resource org/oddjob/script/ScriptFunctions.xml}
 *
 * @author Rob Gordon - Based on the original from Ant.
 */
public class ScriptJob extends SerializableJob implements ConsoleOwner {

    private static final long serialVersionUID = 2020092900L;

    private static final Logger logger = LoggerFactory.getLogger(ScriptJob.class);

    private static final AtomicInteger consoleCount = new AtomicInteger();

    private static String uniqueConsoleId() {
        return ("SCRIPT_CONSOLE" + consoleCount.getAndIncrement());
    }

    private transient LogArchiveImpl consoleArchive;

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
     * @oddjob.required Yes, if script isn't.
     */
    private transient InputStream input;

	/**
	 * @oddjob.property
	 * @oddjob.description The script provided as text.
	 * @oddjob.required Yes, if input isn't.
	 */
    private String script;

    /**
     * @oddjob.property
     * @oddjob.description An input stream which will
     * act as stdin for the script.
     * @oddjob.required No, defaults to none.
     */
    private transient InputStream stdin;

    /**
     * @oddjob.property
     * @oddjob.description An output to where stdout
     * for the script will be written.
     * @oddjob.required No, defaults to none.
     */
    private transient OutputStream stdout;

    /**
     * @oddjob.property
     * @oddjob.description An output to where stderr
     * of the script will be written.
     * @oddjob.required No, defaults to none.
     */
    private transient OutputStream stderr;

    /**
     * @oddjob.property
     * @oddjob.description Combine stdin and stderr.
     * @oddjob.required No.
     */
    private boolean redirectStderr;

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
    private transient volatile Invocable invocable;

    /**
     * So values can be retrieved.
     */
    private transient volatile Bindings bindings;

    /**
     * @oddjob.property
     * @oddjob.description ClassLoader to load the Script Engine.
     * @oddjob.required No. Automatically set to the current Oddjob class loader.
     */
    private transient ClassLoader classLoader;


    public ScriptJob() {
        completeConstruction();
    }

    /**
     * Complete construction.
     */
    private void completeConstruction() {
        consoleArchive = new LogArchiveImpl(
                uniqueConsoleId(), LogArchiver.MAX_HISTORY);
    }


    /*
     *  (non-Javadoc)
     * @see org.oddjob.framework.SimpleJob#execute()
     */
    protected int execute() throws IOException {
        ScriptCompiler compiler = new ScriptCompiler(language,
                classLoader);

		Evaluatable evaluatable;
		if (script == null) {
			if (input == null) {
				throw new OddjobConfigException("No script provided!");
			}
			evaluatable = compiler.compileScript(
					new InputStreamReader(input));
		}
		else {
			if (input != null) {
				throw new OddjobConfigException("Script and Input can't both be specified");
			}
			evaluatable = compiler.compileScript(
					script);
		}

        logger.info("Script compiled.");

        invocable = compiler.getInvocable();

        ScriptContext scriptContext = evaluatable.getScriptContext();

        OutputStream stdout = this.stdout;

        OutputStream stderr;
        if (redirectStderr) {
            stderr = stdout;
        } else {
            stderr = this.stderr;
        }

        InputStream inStream = Optional.ofNullable(this.stdin)
                .orElseGet(() -> DevNullType.IN);

        try (OutputStream outStream = new LoggingOutputStream(stdout,
                LogLevel.INFO, consoleArchive);

             OutputStream errStream = new LoggingOutputStream(stderr,
                     LogLevel.ERROR, consoleArchive)
        ) {
            scriptContext.setReader(new InputStreamReader(inStream));
            scriptContext.setWriter(new OutputStreamWriter(outStream));
            scriptContext.setErrorWriter(new OutputStreamWriter(errStream));

            if (beans == null) {
                scriptContext.setBindings(
                        new SessionBindings(getArooaSession().getBeanRegistry()),
                                ScriptContext.GLOBAL_SCOPE);
            } else {
                scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).putAll(this.beans);
            }
            this.bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);

			try {
				Object result = evaluatable.eval(scriptContext);

				this.result = Optional.ofNullable(this.resultVariable)
						.map(v -> this.bindings.get(v))
						.orElse(result);

				logger.info("Script executed. Result " + result);

			} catch (ScriptException e) {
				throw new OddjobWrapperException(e);
			}
        }

        if (resultForState) {
            if (this.result instanceof Number) {
                return ((Number) this.result).intValue();
            }
        }

        return 0;
    }

    public LogArchive consoleLog() {
        return consoleArchive;
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
     * @param name  The name of the bean.
     * @param value The bean.
     */
    public void setBeans(String name, Object value) {
        if (beans == null) {
            beans = new HashMap<>();
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

	public String getScript() {
		return script;
	}

	@ArooaText
	public void setScript(String script) {
		this.script = script;
	}

	public Invocable getInvocable() {
        return invocable;
    }

    public Function<Object, Object> getFunction(String name) {

        return new Function<Object, Object>() {
            @Override
            public Object apply(Object o) {
                try {
                    return getInvocable().invokeFunction(name, o);
                } catch (ScriptException | NoSuchMethodException e) {
                    throw new IllegalArgumentException(e);
                }
            }

            @Override
            public String toString() {
                return "Function " + name + " on " + ScriptJob.this;
            }
        };
    }


    public boolean isRedirectStderr() {
        return redirectStderr;
    }

    public void setRedirectStderr(boolean redirectStderr) {
        this.redirectStderr = redirectStderr;
    }

    public InputStream getStdin() {
        return stdin;
    }

    public void setStdin(InputStream stdin) {
        this.stdin = stdin;
    }

    public OutputStream getStdout() {
        return stdout;
    }

    public void setStdout(OutputStream stdout) {
        this.stdout = stdout;
    }

    public OutputStream getStderr() {
        return stderr;
    }

    public void setStderr(OutputStream stderr) {
        this.stderr = stderr;
    }

    /**
     * @oddjob.property variables
     * @oddjob.description Provide access to variables declared within the
     * script.
     */
    public Object getVariables(String key) {
		return Optional.ofNullable(this.bindings)
				.map(b -> b.get(key))
				.orElse(null);
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
