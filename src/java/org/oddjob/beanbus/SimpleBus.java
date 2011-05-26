package org.oddjob.beanbus;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class SimpleBus<T> implements BeanBus {
	private static final Logger logger = Logger.getLogger(SimpleBus.class);
	
	private Driver<? extends T> driver;

	private StageNotifier conductor;
	
	private List<BusListener> busListeners = 
		new ArrayList<BusListener>();
	
	@Override
	public void run() {
		if (driver == null) {
			throw new NullPointerException("From.");
		}
		
		if (driver instanceof BusAware) {
			((BusAware) driver).setBus(this);
		}
		
		try {
			fireBusStarting();
			
			driver.go();
			
			fireBusStopping();
		} 
		catch (BusException e) {
			fireBusCrashed(e);
			throw new RuntimeException(e);
		}
		finally {
			
			fireBusTerminated();			
		}
	}

	@Override
	public void stop() {
		driver.stop();
	}
	
	public Driver<? extends T> getDriver() {
		return driver;
	}

	public void setDriver(Driver<? extends T> from) {
		this.driver = from;
		if (conductor == null && driver instanceof StageNotifier) {
			conductor = ((StageNotifier) driver);
		}		
	}

	@Override
	public void addBusListener(BusListener listener) {
		busListeners.add(listener);
	}
	
	@Override
	public void removeBusListener(BusListener listener) {
		busListeners.remove(listener);
	}
	
	@Override
	public void addStageListener(StageListener listener) {
		if (conductor != null) {
			conductor.addStageListener(listener);
		}
	}
	
	@Override
	public void removeStageListener(StageListener listener) {
		if (conductor != null) {
			conductor.removeStageListener(listener);
		}
	}

	protected void fireBusStarting() throws CrashBusException {
		List<BusListener> copy = new ArrayList<BusListener>(busListeners);
		
		BusEvent event = new BusEvent(this);
		
		for (BusListener listener : copy) {
			listener.busStarting(event);
		}
	}
	
	protected void fireBusStopping() throws CrashBusException {
		List<BusListener> copy = new ArrayList<BusListener>(busListeners);
		
		BusEvent event = new BusEvent(this);
		
		for (BusListener listener : copy) {
			listener.busStopping(event);
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

	public StageNotifier getConductor() {
		return conductor;
	}

	public void setConductor(StageNotifier conductor) {
		this.conductor = conductor;
	}
}
