package org.oddjob.framework;

public interface ServiceStrategy {

	ServiceAdaptor serviceFor(Object component);
}
