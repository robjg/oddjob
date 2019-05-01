/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.design.*;
import org.oddjob.arooa.design.etc.FileAttribute;
import org.oddjob.arooa.design.screem.*;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.SessionOverrideContext;
import org.oddjob.arooa.types.InlineType;
import org.oddjob.arooa.utils.MutablesOverrideSession;
import org.oddjob.jobs.structural.ForEachJob;

/**
 *
 */
public class ForEachDC implements DesignFactory {

    public DesignInstance createDesign(ArooaElement element,
                                       ArooaContext parentContext) {

        ArooaSession session = new MutablesOverrideSession(
                parentContext.getSession());

        session.getBeanRegistry().register(
                InlineType.INLINE_CONFIGURATION_DEFINITION,
                InlineType.configurationDefinition(
                        ForEachJob.FOREACH_ELEMENT,
                        new ForEachRootDC()));

        SessionOverrideContext newContext = new SessionOverrideContext(
                parentContext, session);

        return new ForEachDesign(element, newContext);
    }
}


class ForEachDesign extends BaseDC {

    private final SimpleDesignProperty values;

    private final FileAttribute file;

    private final SimpleDesignProperty configuration;

    private final SimpleTextAttribute parallel;

    private final SimpleDesignProperty executorService;

    private final SimpleTextAttribute preLoad;

    private final SimpleTextAttribute purgeAfter;

    ForEachDesign(ArooaElement element, ArooaContext parentContext) {
        super(element, parentContext);

        values = new SimpleDesignProperty(
                "values", this);

        file = new FileAttribute("file", this);

        configuration = new SimpleDesignProperty(
                "configuration", this);

        parallel = new SimpleTextAttribute(
                "parallel", this);

        executorService = new SimpleDesignProperty(
                "executorService", this);

        preLoad = new SimpleTextAttribute("preLoad", this);

        purgeAfter = new SimpleTextAttribute("purgeAfter", this);
    }

    public Form detail() {
        return new StandardForm(this)
                .addFormItem(basePanel())
                .addFormItem(new BorderedGroup("For Each Of")
                        .add(values.view().setTitle("Values")))
                .addFormItem(new BorderedGroup("Configuration Options").add(
                        new FieldSelection()
                                .add(file.view().setTitle("Configuration File"))
                                .add(configuration.view().setTitle("Configuration"))
                ))
                .addFormItem(
                        new TabGroup()
                                .add(new FieldGroup("Parallel")
                                        .add(parallel.view().setTitle("Parallel"))
                                        .add(executorService.view().setTitle("Execution Service")))
                                .add(new FieldGroup("Execution Window")
                                        .add(preLoad.view().setTitle("Pre-Load"))
                                        .add(purgeAfter.view().setTitle("Purge After"))))
                ;
    }

    @Override
    public DesignProperty[] children() {
        return new DesignProperty[]{name, values, file, configuration,
                parallel, executorService, preLoad, purgeAfter};
    }
}
