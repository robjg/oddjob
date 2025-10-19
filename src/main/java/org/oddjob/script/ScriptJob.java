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
import org.oddjob.arooa.registry.BeanRegistry;
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
import javax.script.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @oddjob.description Execute a script. The script can be in any
 * language that supports the
 * <a href="https://docs.oracle.com/en/java/javase/11/scripting/index.html"></a>Java Scripting Framework</a>.
 * The Oddjob distribution comes packaged with <a href="https://openjdk.org/projects/nashorn/">Nashorn</a>.
 * All examples here use Nashorn.
 *
 * <h3>Configuring a Script</h3>
 * The named {@code bind} property allows values to be passed to a script
 * so that they are available as variables.
 * Setting the property {@code bindSession} binds all Oddjob's beans
 * so that they are available as variables. This is equivalent to the variables
 * available <i>#{} expressions</i>.
 *
 * <h3>Script Results</h3>
 * Variables defined within a script may be accessed in several ways.
 * The {@code variables} mapped property may be used to access the variable
 * by name. The {@export} property will export a variable to the oddjob
 * session. The {@exportAll} property will export all variables into the
 * oddjob session. The result of a function can be accessed with the
 * {@code result} property. Some scripts don't return a result, in which case
 * the {@code resultVariable} property can be used to take the result from
 * a variable. If the {@code resultForState} property is true then the
 * result will be used to set the Completion State from the variable, 0 for
 * Success, otherwise Failure.
 *
 * <h3>Input and Output</h3>
 * Script input and output can be configured using the properties {@code stdin}
 * {@code stdout}, {@code stderr} and {@code redirectStderr}.
 * In Oddjob Explorer, a scripts output is captured in the console tab for
 * that job.
 *
 * @oddjob.example Hello World.
 * {@oddjob.xml.resource org/oddjob/script/ScriptHelloWorld.xml}
 *
 * @oddjob.example Variables from and to Oddjob.
 * {@oddjob.xml.resource org/oddjob/script/VariablesFromAndToOddjob.xml}
 *
 * @oddjob.example Binding and exporting to Oddjob's session. The Variables
 * job uses Identify to insert the 'Apple' into Oddjob's session with the
 * name fruit. {@code bindSession} causes this to be available to script,
 * where it is assigned to the 'snack' variable. The script also defines
 * an add function. The {@code exportAll} property causes both these to
 * be exported to Oddjob. The two echo jobs show how these variables are
 * now available in Oddjob's session. When the script job is reset, the
 * variables are removed from the session.
 * {@oddjob.xml.resource org/oddjob/script/ScriptUseSessionBindings.xml}
 *
 * @oddjob.example Using a script to set a property on a Job elsewhere in Oddjob.
 * {@oddjob.xml.resource org/oddjob/script/ScriptSettingProperty.xml}
 *
 * @oddjob.example Invoking a script to provide a substring function.
 * {@oddjob.xml.resource org/oddjob/script/InvokeScriptFunction.xml}
 *
 * @oddjob.example Setting the script job to not complete. The result of the script
 * is used to set the state. 0 for Success, anything else for Incomplete.
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
    private transient Map<String, Object> bind;

    /**
     * @oddjob.property
     * @oddjob.description Make all Oddjob's components available to the script as bindings.
     * @oddjob.required No.
     */
    private transient boolean bindSession;

    /**
     * @oddjob.property
     * @oddjob.description Export bindings from the engine into the session
     * using the given name.
     * The first entry is the name of the binding, the second is the name.
     * Repeating the name is tedious so the '.' character can be used to
     * specify the same name as the binding.
     * @oddjob.required No.
     */
    private transient Map<String, String> export;

    /**
     * @oddjob.property
     * @oddjob.description Export all bindings from the engine into Oddjob's
     * session.
     * @oddjob.required No.
     */
    private transient boolean exportAll;

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
     * @oddjob.required Read only.
     */
    private transient volatile Invocable invocable;

    /**
     * @oddjob.property
     * @oddjob.description ClassLoader to load the Script Engine.
     * @oddjob.required No. Automatically set to the current Oddjob class loader.
     */
    private transient ClassLoader classLoader;

    /** So values can be retrieved. */
    private transient volatile Bindings bindings;

    /** Variable exported to the session. So they can be removed on reset. */
    private transient volatile List<Object> exported;

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

        // Nashorn seems to use the Thread Context Classloader so set it for
        // the duration of the execution.
        ClassLoader existing = Thread.currentThread().getContextClassLoader();
        Optional.ofNullable(this.classLoader)
                .ifPresent(Thread.currentThread()::setContextClassLoader);
        try {
            return _execute();
        } finally {
            Thread.currentThread().setContextClassLoader(existing);
        }
    }

    protected int _execute() throws IOException {
        ScriptCompiler compiler = new ScriptCompiler(language,
                classLoader);

        Evaluatable evaluatable;
        if (script == null) {
            if (input == null) {
                throw new OddjobConfigException("No script provided!");
            }
            evaluatable = compiler.compileScript(
                    new InputStreamReader(input));
        } else {
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

            Bindings sessionBindings = null;
            BeanRegistry beanRegistry = getArooaSession().getBeanRegistry();
            if (bindSession) {
                sessionBindings = new SessionBindings(beanRegistry);
            }
            if (bind != null) {
                SimpleBindings simpleBindings = new SimpleBindings(this.bind);
                if (sessionBindings == null) {
                    sessionBindings = simpleBindings;
                } else {
                    sessionBindings = CompositeBindings.of(simpleBindings, sessionBindings);
                }
            }
            if (sessionBindings != null) {
                scriptContext.setBindings(sessionBindings,
                        ScriptContext.GLOBAL_SCOPE);
            }

            try {
                Object result = evaluatable.eval(scriptContext);

                this.bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);

                this.result = Optional.ofNullable(this.resultVariable)
                        .map(v -> this.bindings.get(v))
                        .orElse(result);

                logger.info("Script executed. Result {}", result);


            } catch (ScriptException e) {
                throw new OddjobWrapperException(e);
            }

            exported = new ArrayList<>();
            if (export != null) {
                for (Map.Entry<String, String> entry : export.entrySet()) {
                    Object value = this.bindings.get(entry.getKey());
                    if (value == null) {
                        logger.info("Ignoring export {} as binding is null.", entry.getKey());
                    } else {
                        beanRegistry.register(entry.getValue(), value);
                        exported.add(value);
                        logger.info("Exported {} as {}={}", entry.getKey(), entry.getValue(), value);
                    }
                }
            }

            if (exportAll) {
                for (Map.Entry<String, Object> entry : this.bindings.entrySet()) {
                    Object value = entry.getValue();
                    beanRegistry.register(entry.getKey(), value);
                    exported.add(value);
                    logger.info("Exported {}={}", entry.getKey(), value);
                }
            }

        }

        this.bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);

        if (resultForState) {
            Object result = this.result;
            if (result instanceof Number) {
                return ((Number) result).intValue();
            } else {
                logger.warn("Can't us result {} for state as it is not a number, it is a {}",
                        result, result == null ? "null" : result.getClass().getName());
            }
        }

        return 0;
    }

    @Override
    protected void onReset() {
        this.bind = null;
        this.export = null;
        this.bindings = null;

        if (exported == null || exported.isEmpty()) {
            return;
        }

        BeanRegistry registry = getArooaSession().getBeanRegistry();
        for (Object object : exported) {
            registry.remove(object);
            logger.debug("Session Bindings removed: {}", exported);
        }
        exported = null;
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
     * Get a binding.
     *
     * @param name The name of the binding
     * @return The bean or null if it doesn't exist.
     */
    public Object getBind(String name) {
        if (bind == null) {
            return null;
        }
        return bind.get(name);
    }

    /**
     * Add a binding.
     *
     * @param name  The name of the binding.
     * @param value The thing to be bound.
     */
    public void setBind(String name, Object value) {
        if (bind == null) {
            bind = new HashMap<>();
        }
        logger().debug("Adding binding ({}, [{}]", name, value);
        bind.put(name, value);
    }

    /**
     * Get the named bean.
     *
     * @param name The name of the bean
     * @return The bean or null if it doesn't exist.
     */
    @Deprecated
    public Object getBeans(String name) {
        logger.warn("Beans is deprecated");
        return getBind(name);
    }

    /**
     * @oddjob.property
     * @oddjob.description Deprecated. Use {@code bind} instead.
     *
     * @param name  The name of the bean.
     * @param value The bean.
     */
    @Deprecated
    public void setBeans(String name, Object value) {
        logger().warn("setBeans is Deprecated, use setBind for ({}, [{}]", name, value);
        setBind(name, value);
    }

    public boolean isBindSession() {
        return bindSession;
    }

    public void setBindSession(boolean bindSession) {
        this.bindSession = bindSession;
    }

    /**
     * Get the name of the export binding.
     *
     * @param name The name of the export binding
     * @return The name or null if it doesn't exist.
     */
    public String getExport(String name) {
        if (export == null) {
            return null;
        }
        return export.get(name);
    }

    /**
     * Export a binding of the first name into the session with the
     * second name.
     *
     * @param name  The name of the binding.
     * @param value The name of the export.
     */
    public void setExport(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("No name");
        }
        if (value == null) {
            return;
        }
        if (".".equals(value)) {
            value = name;
        }
        if (export == null) {
            export = new HashMap<>();
        }
        logger().debug("Adding export ({}, [{}]", name, value);
        export.put(name, value);
    }

    public boolean isExportAll() {
        return exportAll;
    }

    public void setExportAll(boolean exportAll) {
        this.exportAll = exportAll;
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

    /**
     * @oddjob.property
     * @oddjob.description Adapts a script function type to a Java Function.
     * This isn't really necessary with Nashorn as there is a built in Conversion to do this.
     * May be useful for other script engines.
     * @oddjob.required Read Only.
     *
     * @param name The name of the script function.
     * @return A Java Function.
     */
    public Function<Object, Object> getFunction(String name) {

        return new Function<>() {
            @Override
            public Object apply(Object o) {
                try {
                    if (o instanceof Object[]) {
                        Object[] oa = (Object[]) o;
                        return getInvocable().invokeFunction(name, oa);
                    } else {
                        return getInvocable().invokeFunction(name, o);
                    }
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
     * @oddjob.description Deprecated. Use {@code variable} instead.
     * @oddjob.required Read Only.
     */
    @Deprecated
    public Object getVariables(String key) {
        return Optional.ofNullable(this.bindings)
                .map(b -> b.get(key))
                .orElse(null);
    }

    /**
     * @oddjob.description Provide access to variables declared within the
     * script.
     * @oddjob.required Read Only.
     */
    public Object getVariable(String key) {
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
