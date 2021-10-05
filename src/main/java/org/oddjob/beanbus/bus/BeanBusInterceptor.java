package org.oddjob.beanbus.bus;

import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ParsingInterceptor;
import org.oddjob.arooa.life.ClassResolverClassLoader;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.SessionOverrideContext;
import org.oddjob.beanbus.mega.MegaBeanBus;

/**
 * A {@link ParsingInterceptor} provided by a {@link MegaBeanBus} to
 * provide specialised parsing of the configuration to
 * allow bean bus elements to be used.
 *
 * @author rob
 */
public class BeanBusInterceptor implements ParsingInterceptor {

    @Override
    public ArooaContext intercept(ArooaContext suggestedContext)
            throws ArooaConfigurationException {

        ArooaSession existingSession = suggestedContext.getSession();

        ArooaSession session = new BusSessionFactory().createSession(
                existingSession,
                new ClassResolverClassLoader(existingSession
                        .getArooaDescriptor()
                        .getClassResolver()));

        return new SessionOverrideContext(suggestedContext, session);
    }

}
