package org.oddjob.beanbus;

import org.oddjob.arooa.registry.ServiceProvider;

/**
 * Something that provides a {@link ConductorService}.
 *
 * @author rob
 */
public interface ConductorServiceProvider extends ServiceProvider {

    @Override
    ConductorService getServices();
}
