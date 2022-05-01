/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.beanbus.adapt;

import org.oddjob.beanbus.bus.BasicBusService;
import org.oddjob.framework.adapt.service.ServiceAdaptor;
import org.oddjob.framework.adapt.service.ServiceWrapper;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.state.ServiceState;
import org.oddjob.util.Restore;

import java.util.function.Consumer;

/**
 * Wraps a Collection object so that it can be added to an 
 * {@link BasicBusService}.
 * <p>
 * 
 * @author Rob Gordon.
 */
public class ConsumerWrapper<E> extends ServiceWrapper
implements Consumer<E> {

	private final Consumer<E> consumer;

    /**
     * Constructor.
     * 
     * @param consumer
     * @param proxy
     */
    public ConsumerWrapper(ServiceAdaptor serviceAdaptor, Consumer<E> consumer, Object proxy) {
    	super(serviceAdaptor, proxy);
    	this.consumer = consumer;
    }

	@Override
	public void accept(E bean) {
		try (Restore restore = ComponentBoundary.push(loggerName(), consumer)) {

			if (stateHandler().getState() == ServiceState.STARTED) {
				consumer.accept(bean);
			}
			else {
				logger().warn("Ignoring because service not started: {}", bean);
			}
		}
		catch (Exception ex) {
			logger().error("Exception processing bean: {}", bean, ex);
			stateHandler().runLocked(() -> getStateChanger().setStateException(ex));
		}
	}

}
