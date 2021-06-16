package org.oddjob.beanbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for {@link BusConductor}s. Provides methods for firing 
 * events.
 * 
 * @author rob
 *
 */
abstract public class AbstractBusConductor implements BusConductor {

	private static final Logger logger = LoggerFactory.getLogger(AbstractBusConductor.class);
	
	private final List<BusListener> busListeners =
			new ArrayList<>();
	
	@Override
	public void addBusListener(BusListener listener) {
		busListeners.add(listener);
	}
	
	@Override
	public void removeBusListener(BusListener listener) {
		busListeners.remove(listener);
	}
	
	protected void fireBusStarting() throws BusCrashException {
		List<BusListener> copy = new ArrayList<>(busListeners);
		
		BusEvent event = new BusEvent(this, BusPhase.BUS_STARTING);
		
		for (BusListener listener : copy) {
			listener.busStarting(event);
		}
	}
	
	protected void fireTripBeginning() {
		List<BusListener> copy = new ArrayList<>(busListeners);
		
		BusEvent event = new BusEvent(this, BusPhase.TRIP_BEGINNING);
		
		for (BusListener listener : copy) {
			listener.tripBeginning(event);
		}
	}
	
	protected void fireTripEnding()  {
		List<BusListener> copy = new ArrayList<>(busListeners);
		
		BusEvent event = new BusEvent(this, BusPhase.TRIP_ENDING);
		
		for (BusListener listener : copy) {
			listener.tripEnding(event);
		}
	}
	
	protected void fireBusStopRequested(boolean started) {
		List<BusListener> copy = new ArrayList<>(busListeners);
		
		BusEvent event = new BusEvent(this, 
				started ? BusPhase.BUS_RUNNING : BusPhase.BUS_STOPPED);
		
		for (BusListener listener : copy) {
			listener.busStopRequested(event);
		}
	}
	
	protected void fireBusStopping() throws BusCrashException {
		List<BusListener> copy = new ArrayList<>(busListeners);
		
		BusEvent event = new BusEvent(this, BusPhase.BUS_STOPPING);

		BusCrashException busCrashException = null;
		
		for (BusListener listener : copy) {
			try {
				listener.busStopping(event);
			}
			catch (BusCrashException e) {
				busCrashException = e;
			}
		}
		if (busCrashException != null) {
			throw busCrashException;
		}
	}
	
	protected void fireBusTerminated() {
		List<BusListener> copy = new ArrayList<>(busListeners);
		
		BusEvent event = new BusEvent(this, BusPhase.BUS_STOPPED);
		
		for (BusListener listener : copy) {
			try {
				listener.busTerminated(event);
			}
			catch (Throwable t) {
				logger.info("Exception from Listener [" 
						+ listener + "]", t);
			}
		}
	}
	
	protected void fireBusCrashed(BusPhase phase, Exception e) {
		List<BusListener> copy = new ArrayList<>(busListeners);
		
		BusEvent event = new BusEvent(this, phase, e);
		
		for (BusListener listener : copy) {
			try {
				listener.busCrashed(event);
			}
			catch (Throwable t) {
				logger.info("Exception from Listener [" 
						+ listener + "]", t);
			}
		}
	}

	@Override
	abstract public String toString();
}
