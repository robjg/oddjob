/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.beanbus.mega;

import org.oddjob.beanbus.*;
import org.oddjob.framework.adapt.service.ServiceAdaptor;
import org.oddjob.framework.adapt.service.ServiceWrapper;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.util.Restore;

import java.util.function.Consumer;

/**
 * Wraps a Collection object so that it can be added to an 
 * {@link MegaBeanBus}. 
 * <p>
 * 
 * @author Rob Gordon.
 */
public class ConsumerWrapper<E> extends ServiceWrapper
implements Consumer<E>, BusPart {

	private final Consumer<E> consumer;

    private final TrackingBusListener busListener = 
    		new TrackingBusListener() {
		
    	@Override
    	public void busCrashed(BusEvent event) {
    		busCrashException = event.getBusCrashException();
    	}

				@Override
		public void busTerminated(BusEvent event) {
		}
		
		@Override
		public void busStarting(BusEvent event) {
			busCrashException = null;
		}
		
	};
    
	/** A job that isn't a bus service won't know the bus has crashed. */
	private volatile Exception busCrashException;

    /**
     * Constructor.
     * 
     * @param collection
     * @param proxy
     */
    public ConsumerWrapper(ServiceAdaptor serviceAdaptor, Consumer<E> collection, Object proxy) {
    	super(serviceAdaptor, proxy);
    	this.consumer = collection;
    }

	@Override
	public void onDestroy() {
		busListener.setBusConductor(null);
		super.onDestroy();
	}

	@Override
    public void prepare(BusConductor busConductor) {
    	
		try (Restore restore = ComponentBoundary.push(loggerName(), getWrapped())) {

			busListener.setBusConductor(busConductor);

			logger().info("Prepared with Bus Conductor [" + busConductor + "]");
		}
    }
        	
	@Override
	public BusConductor conductorForService(BusConductor busConductor) {
		return new LoggingBusConductorFilter(busConductor);
	}
	
	// Consumer Methods
	//

	@Override
	public void accept(E e) {
		if (busCrashException != null) {
			throw new RuntimeException(busCrashException);
		}
		try (Restore restore = ComponentBoundary.push(loggerName(), consumer)) {
			consumer.accept(e);
		}
	}

	class LoggingBusConductorFilter extends BusConductorFilter {
		
		public LoggingBusConductorFilter(BusConductor conductor) {
			super(conductor);
		}
		
		@Override
		protected void busStarting(BusEvent event,
				BusListener listener) throws BusCrashException {
			try (Restore restore = ComponentBoundary.push(loggerName(), consumer)) {
				super.busStarting(event, listener);
			}
		}
		
		@Override
		protected void tripBeginning(BusEvent event,
				BusListener listener) throws BusCrashException {
			try (Restore restore = ComponentBoundary.push(loggerName(), consumer)) {
				super.tripBeginning(event, listener);
			}
		}
		
		@Override
		protected void tripEnding(BusEvent event,
				BusListener listener) throws BusCrashException {
			try (Restore restore = ComponentBoundary.push(loggerName(), consumer)) {
				super.tripEnding(event, listener);
			}
		}
		
		@Override
		protected void busStopRequested(BusEvent event,
				BusListener listener) {
			try (Restore restore = ComponentBoundary.push(loggerName(), consumer)) {
				super.busStopRequested(event, listener);
			}
		}
		
		@Override
		protected void busStopping(BusEvent event,
				BusListener listener) throws BusCrashException {
			try (Restore restore = ComponentBoundary.push(loggerName(), consumer)) {
				super.busStopping(event, listener);
			}
		}
		
		@Override
		protected void busCrashed(BusEvent event,
				BusListener listener) {
			try (Restore restore = ComponentBoundary.push(loggerName(), consumer)) {
				super.busCrashed(event, listener);
			}
		}
		
		@Override
		protected void busTerminated(BusEvent event,
				BusListener listener) {
			try (Restore restore = ComponentBoundary.push(loggerName(), consumer)) {
				super.busTerminated(event, listener);
			}
		}
		
	}
}
