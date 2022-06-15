package org.oddjob.framework.adapt.service;

import org.oddjob.framework.AsyncJob;
import org.oddjob.framework.AsyncService;
import org.oddjob.framework.FallibleComponent;
import org.oddjob.framework.Service;
import org.oddjob.framework.adapt.ComponentAdapter;

import java.util.Optional;

/**
 * Something that adapts a component to be a {@link Service}.
 *  
 * @author rob
 *
 */
public interface ServiceAdaptor 
extends Service, FallibleComponent, ComponentAdapter {

    /**
     * Possibly create an {@link AsyncJob}.
     **
     * @return Possibly an AsyncJob.
     */
    Optional<AsyncService> asAsync();
}
