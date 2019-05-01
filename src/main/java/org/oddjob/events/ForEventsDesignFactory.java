package org.oddjob.events;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.GenericDesignFactory;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.SessionOverrideContext;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.types.InlineType;
import org.oddjob.arooa.utils.MutablesOverrideSession;
import org.oddjob.designer.components.ForEachRootDC;

/**
 * A {@link org.oddjob.arooa.design.DesignFactory} for {@link ForEvents}. Provides an
 * {@link org.oddjob.arooa.types.InlineType.ConfigurationDefinition} to enable the
 * {@link InlineType} for configuration.
 */
public class ForEventsDesignFactory extends GenericDesignFactory {

    public ForEventsDesignFactory() {
        super(new SimpleArooaClass(ForEvents.class));
    }

    @Override
    public DesignInstance createDesign(ArooaElement element, ArooaContext parentContext) throws ArooaPropertyException {

        ArooaSession session = new MutablesOverrideSession(
                parentContext.getSession());

        session.getBeanRegistry().register(
                InlineType.INLINE_CONFIGURATION_DEFINITION,
                InlineType.configurationDefinition(
                        ForEvents.FOREACH_ELEMENT,
                        new ForEachRootDC()));

        SessionOverrideContext newContext = new SessionOverrideContext(
                parentContext, session);

        return super.createDesign(element, newContext);
    }
}
