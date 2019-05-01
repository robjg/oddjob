/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.Oddjob;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.design.*;
import org.oddjob.arooa.design.etc.FileAttribute;
import org.oddjob.arooa.design.screem.*;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.SessionOverrideContext;
import org.oddjob.arooa.types.InlineType;
import org.oddjob.arooa.utils.MutablesOverrideSession;

/**
 *
 */
public class OddjobDC implements DesignFactory {

    public DesignInstance createDesign(ArooaElement element,
                                       ArooaContext parentContext) {

        ArooaSession session = new MutablesOverrideSession(
                parentContext.getSession());

        session.getBeanRegistry().register(
                InlineType.INLINE_CONFIGURATION_DEFINITION,
                InlineType.configurationDefinition(
                        Oddjob.ODDJOB_ELEMENT,
                        new ForEachRootDC()));

        SessionOverrideContext newContext = new SessionOverrideContext(
                parentContext, session);

        return new OddjobDesign(element, newContext);
    }
}

class OddjobDesign extends BaseDC {

    private final FileAttribute file;

    private final SimpleDesignProperty configuration;

    private final SimpleDesignProperty args;

    private final MappedDesignProperty export;

    private final SimpleDesignProperty properties;

    private final SimpleTextAttribute inheritance;

    private final SimpleDesignProperty descriptorFactory;

    private final SimpleDesignProperty classLoader;

    private final SimpleDesignProperty persister;

    OddjobDesign(ArooaElement element, ArooaContext parentContext) {
        super(element, parentContext);

        file = new FileAttribute("file", this);

        configuration = new SimpleDesignProperty("configuration", this);

        args = new SimpleDesignProperty("args", this);

        export = new MappedDesignProperty("export", this);

        properties = new SimpleDesignProperty("properties", this);

        inheritance = new SimpleTextAttribute("inheritance", this);

        descriptorFactory = new SimpleDesignProperty("descriptorFactory", this);

        classLoader = new SimpleDesignProperty("classLoader", this);

        persister = new SimpleDesignProperty("persister", this);

    }

    public Form detail() {
        return new StandardForm(this)
                .addFormItem(basePanel())
                .addFormItem(new BorderedGroup("Configuration Options").add(
                        new FieldSelection()
                                .add(file.view().setTitle("Configuration File"))
                                .add(configuration.view().setTitle("Configuration"))
                ))
                .addFormItem(
                        new TabGroup()
                                .add(new FieldGroup("Export")
                                        .add(args.view().setTitle("Arguments"))
                                        .add(properties.view().setTitle("Properties"))
                                        .add(inheritance.view().setTitle("Inheritance"))
                                        .add(export.view().setTitle("Export"))
                                )
                                .add(new FieldGroup("Advanced")
                                        .add(descriptorFactory.view().setTitle("Descriptor"))
                                        .add(classLoader.view().setTitle("ClassLoader"))
                                        .add(persister.view().setTitle("Persister"))
                                )
                );
    }

    @Override
    public DesignProperty[] children() {
        return new DesignProperty[]{
                name,
                file, configuration,
                args, properties, inheritance, export,
                descriptorFactory, classLoader, persister};
    }
}
