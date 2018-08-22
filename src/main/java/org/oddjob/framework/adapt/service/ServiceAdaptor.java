package org.oddjob.framework.adapt.service;

import org.oddjob.framework.FallibleComponent;
import org.oddjob.framework.Service;
import org.oddjob.framework.adapt.ComponentAdapter;

/**
 * Something that adapts a component to be a {@link Service}.
 *  
 * @author rob
 *
 */
public interface ServiceAdaptor 
extends Service, FallibleComponent, ComponentAdapter {

}
