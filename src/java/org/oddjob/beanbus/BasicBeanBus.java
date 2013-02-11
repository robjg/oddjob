package org.oddjob.beanbus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

public class BasicBeanBus<T> implements BusConductor, BeanBus<T> {
	private static final Logger logger = Logger.getLogger(BasicBeanBus.class);

	private final List<BusListener> busListeners = 
			new ArrayList<BusListener>();
	
	private Collection<? super T> to;

	private boolean started = false;
	
	private boolean tripping = false;
	
	private final BeanBusCommand stopBusCommand;
	
	public BasicBeanBus() {
		this(null);
	}
	
	public BasicBeanBus(BeanBusCommand stopBusCommand) {
		this.stopBusCommand = stopBusCommand;
	}
	
	@Override
	public void startBus() throws BusCrashException {
		
		if (started) {
			throw new IllegalStateException("Bus already started.");
		}
		
		fireBusStarting();
		
		started = true;
	}
	
	@Override
	public void stopBus() throws BusCrashException {
		
		if (!started) {
			throw new IllegalStateException("Bus not started.");
		}
		
		if (tripping) {
			tripEnd();
		}
		
		try {
			fireBusStopping();
		}
		finally {
			terminateBus();
		}
	}		
	
	@Override
	public void accept(T bean) throws BusCrashException {
		
		if (!started) {
			throw new IllegalStateException("Bus not started.");
		}
		
		if (!tripping) {
			tripBegin();
		}
		
		try {
			if (to != null) {
				to.add(bean);
			}
		}
		catch (IllegalArgumentException e) {
			
			BusCrashException e2 = new BusCrashException("Unhandled " + 
					IllegalArgumentException.class.getName(), e);
			
			crashBus(e2);
			
			throw e2;
		}
		catch (RuntimeException e) {
			
			BusCrashException e2 = new BusCrashException(e);
			
			crashBus(e2);
			
			throw e2;
		}
		
	}
	
	@Override
	public void cleanBus() throws BusCrashException {
		
		// should it be an exception to clean a bus that hasn't had beans?
		// No - because to components could both ask to clean the bus
		// between trips.
		if (tripping) {
			tripEnd();
		}
		
	}
	
	@Override
	public void requestBusStop() throws BusCrashException {
		if (stopBusCommand != null) {
			stopBusCommand.run();
		}		
	}
	
	private void tripBegin() throws BusCrashException {
		
		try {
			onTripBegin();
			
			tripping = true;
			
			fireTripBeginning();			
		}
		catch (BusCrashException e) {
			crashBus(e);
			
			throw e;
		}
	}
	
	private void tripEnd() throws BusCrashException {
		
		try {
			fireTripEnding();
			
			tripping = false;
			
			onTripEnd();
		}
		catch (BusCrashException e) {
			crashBus(e);
			
			throw e;
		}
	}
	
	private void crashBus(BusCrashException e) {
	
		try {
			fireBusCrashed(e);
		}
		finally {
			terminateBus();
		}
	}
	
	private void terminateBus() {
		
		started = false;
		
		fireBusTerminated();
	}
	
	protected void onTripBegin() {
		
	}
	
	protected void onTripEnd() {
		
	}
	
	
	protected void onBusCrash() {
		
	}
	
	@Override
	public void addBusListener(BusListener listener) {
		busListeners.add(listener);
	}
	
	@Override
	public void removeBusListener(BusListener listener) {
		busListeners.remove(listener);
	}
	
	protected void fireBusStarting() throws BusCrashException {
		List<BusListener> copy = new ArrayList<BusListener>(busListeners);
		
		BusEvent event = new BusEvent(this);
		
		for (BusListener listener : copy) {
			listener.busStarting(event);
		}
	}
	
	protected void fireTripBeginning() throws BusCrashException {
		List<BusListener> copy = new ArrayList<BusListener>(busListeners);
		
		BusEvent event = new BusEvent(this);
		
		for (BusListener listener : copy) {
			listener.tripBeginning(event);
		}
	}
	
	protected void fireTripEnding() throws BusCrashException {
		List<BusListener> copy = new ArrayList<BusListener>(busListeners);
		
		BusEvent event = new BusEvent(this);
		
		for (BusListener listener : copy) {
			listener.tripEnding(event);
		}
	}
	
	protected void fireBusStopping() throws BusCrashException {
		List<BusListener> copy = new ArrayList<BusListener>(busListeners);
		
		BusEvent event = new BusEvent(this);

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
		List<BusListener> copy = new ArrayList<BusListener>(busListeners);
		
		BusEvent event = new BusEvent(this);
		
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
	
	protected void fireBusCrashed(BusException e) {
		List<BusListener> copy = new ArrayList<BusListener>(busListeners);
		
		BusEvent event = new BusEvent(this);
		
		for (BusListener listener : copy) {
			try {
				listener.busCrashed(event, e);
			}
			catch (Throwable t) {
				logger.info("Exception from Listener [" 
						+ listener + "]", t);
			}
		}
	}

	public Collection<? super T> getTo() {
		return to;
	}

	public void setTo(Collection<? super T> to) {
		this.to = to;
	}

	public BeanBusCommand getStopBusCommand() {
		return stopBusCommand;
	}

}
