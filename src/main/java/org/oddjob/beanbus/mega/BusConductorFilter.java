package org.oddjob.beanbus.mega;

import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusListener;

import java.util.HashMap;
import java.util.Map;

public class BusConductorFilter implements BusConductor {

	private final Map<BusListener, BusListener> listeners =
			new HashMap<>();
	
	private final BusConductor original;
	
	public BusConductorFilter(BusConductor original) {
		this.original = original;
	}
	
	protected void busStarting(BusEvent event,
			BusListener listener) throws BusCrashException {
		listener.busStarting(event);
	}
	
	protected void tripBeginning(BusEvent event,
			BusListener listener) {
		listener.tripBeginning(event);
	}
	
	protected void tripEnding(BusEvent event,
			BusListener listener) {
		listener.tripEnding(event);
	}
	
	protected void busStopRequested(BusEvent event,
			BusListener listener) {
		listener.busStopRequested(event);
	}
	
	protected void busStopping(BusEvent event,
			BusListener listener) throws BusCrashException {
		listener.busStopping(event);
	}
	
	protected void busCrashed(BusEvent event,
			BusListener listener) {
		listener.busCrashed(event);
	}
	
	protected void busTerminated(BusEvent event,
			BusListener listener) {
		listener.busTerminated(event);
	}
	
	@Override
	public void addBusListener(final BusListener listener) {
		BusListener filteredListener = new BusListener() {
			
			@Override
			public void tripEnding(BusEvent event) {
				BusConductorFilter.this.tripEnding(event, listener);
			}
			
			@Override
			public void tripBeginning(BusEvent event) {
				BusConductorFilter.this.tripBeginning(event, listener);
			}
			
			@Override
			public void busTerminated(BusEvent event) {
				BusConductorFilter.this.busTerminated(event, listener);
			}
			
			@Override
			public void busStopping(BusEvent event) throws BusCrashException {
				BusConductorFilter.this.busStopping(event, listener);
			}
			
			@Override
			public void busStopRequested(BusEvent event) {
				BusConductorFilter.this.busStopRequested(event, listener);
			}
			
			@Override
			public void busStarting(BusEvent event) throws BusCrashException {
				BusConductorFilter.this.busStarting(event, listener);
			}
			
			@Override
			public void busCrashed(BusEvent event) {
				BusConductorFilter.this.busCrashed(event, listener);
			}
		};
		
		listeners.put(listener, filteredListener);
		
		original.addBusListener(filteredListener);
	}
	
	@Override
	public void removeBusListener(BusListener listener) {
		
		BusListener filteredListener = listeners.remove(listener);
		
		original.removeBusListener(filteredListener);
	}
	
	@Override
	public void flush() {
		original.flush();
	}
	
	@Override
	public void close() {
		original.close();
	}
}
