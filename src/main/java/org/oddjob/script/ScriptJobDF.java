/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.script;

import org.oddjob.arooa.design.*;
import org.oddjob.arooa.design.screem.*;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.designer.components.BaseDC;

/**
 * DesignFactory for the {@link ScriptJob}.
 */
public class ScriptJobDF implements DesignFactory {

    public DesignInstance createDesign(ArooaElement element,
                                       ArooaContext parentContext) {

        return new ScriptDesign(element, parentContext);
    }
}

class ScriptDesign extends BaseDC {

    private final SimpleTextAttribute language;

    private final SimpleTextAttribute resultVariable;

    private final SimpleTextAttribute resultForState;

    private final SimpleTextProperty script;

    private final SimpleDesignProperty input;

    private final MappedDesignProperty bind;

    private final SimpleTextAttribute bindSession;

    private final MappedDesignProperty export;

    private final SimpleTextAttribute exportAll;

    private final SimpleDesignProperty classLoader;

    private final SimpleTextAttribute redirectStderr;

    private final SimpleDesignProperty stdin;

    private final SimpleDesignProperty stdout;

    private final SimpleDesignProperty stderr;

    public ScriptDesign(ArooaElement element, ArooaContext parentContext) {
        super(element, parentContext);

        language = new SimpleTextAttribute("language", this);

        resultVariable = new SimpleTextAttribute("resultVariable", this);

        resultForState = new SimpleTextAttribute("resultForState", this);

        script = new SimpleTextProperty("command");

        input = new SimpleDesignProperty(
                "input", this);

        bind = new MappedDesignProperty(
                "bind", this);

        bindSession = new SimpleTextAttribute("bindSession", this);

        export = new MappedDesignProperty(
                "export", this);

        exportAll = new SimpleTextAttribute("exportAll", this);

        classLoader = new SimpleDesignProperty(
                "classLoader", this);

        redirectStderr = new SimpleTextAttribute("redirectStderr", this);

        stdin = new SimpleDesignProperty(
                "stdin", this);

        stdout = new SimpleDesignProperty(
                "stdout", this);

        stderr = new SimpleDesignProperty(
                "stderr", this);
    }

    /*
     *  (non-Javadoc)
     * @see org.oddjob.designer.model.DesignComponent#form()
     */
    public Form detail() {
        return new StandardForm(this)
                .addFormItem(basePanel())
                .addFormItem(
                        new TabGroup()
                                .add(new FieldGroup("Script")
                                        .add(new FieldSelection()
                                                .add(script.view().setTitle("Script"))
                                                .add(input.view().setTitle("Input"))
                                        )
                                )
                                .add(new FieldGroup("Details")
                                        .add(language.view().setTitle("Language"))
                                        .add(classLoader.view().setTitle("Class Loader"))
                                )
                                .add(new FieldGroup("Bind")
                                        .add(bind.view().setTitle("Bindings"))
                                        .add(bindSession.view().setTitle("Bind Session"))
                                )
                                .add(new FieldGroup("Export")
                                        .add(export.view().setTitle("Export"))
                                        .add(exportAll.view().setTitle("Export All"))
                                        .add(resultVariable.view().setTitle("Result Variable"))
                                        .add(resultForState.view().setTitle("Result For State"))
                                )
                                .add(new FieldGroup("I/O")
                                        .add(redirectStderr.view().setTitle("Redirect Stderr"))
                                        .add(stdin.view().setTitle("Stdin"))
                                        .add(stdout.view().setTitle("Stdout"))
                                        .add(stderr.view().setTitle("Stderr"))
                                )
                );
    }

    @Override
    public DesignProperty[] children() {
        return new DesignProperty[]{
                name, script, input,
                language, classLoader,
                bind, bindSession,
                export, exportAll, resultVariable, resultForState,
                redirectStderr, stdin, stdout, stderr};
    }
}
