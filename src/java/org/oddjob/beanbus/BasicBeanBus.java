package org.oddjob.beanbus;

import java.util.Collection;

import org.apache.log4j.Logger;

public class BasicBeanBus<T> extends AbstractDestination<T>
implements BeanBus<T> {
	private static final Logger logger = Logger.getLogger(BasicBeanBus.class);

	private volatile Collection<? super T> to;

	private boolean started = false;
	
	private boolean tripping = false;
	
	private final Runnable stopBusCommand;
	
	private final AbstractBusConductor busConductor = 
			new AbstractBusConductor() {
		
		@Override
		public void requestBusStop() {
			busConductor.fireBusStopRequested(started);
			
			if (stopBusCommand != null) {
				stopBusCommand.run();
			}		
		}
		
		@Override
		public void cleanBus() throws BusCrashException {
			// should it be an exception to clean a bus that hasn't had beans?
			// No - because two components could both ask to clean the bus
			// between trips.
			if (tripping) {
				tripEnd();
			}		
		}
		
		@Override
		public String toString() {
			return BusConductor.class.getSimpleName() + " for " +
					BasicBeanBus.class.getSimpleName();
		}
	};
	
	/**
	 * Constructor for an unstoppable bus.
	 */
	public BasicBeanBus() {
		this(null);
	}
	
	/**
	 * Constructor for a stoppable bus.
	 * 
	 * @param stopBusCommand
	 */
	public BasicBeanBus(Runnable stopBusCommand) {
		this.stopBusCommand = stopBusCommand;
	}
	
	@Override
	public void startBus() throws BusCrashException {
		
		if (started) {
			throw new IllegalStateException("Bus already started.");
		}
		
		try {
			busConductor.fireBusStarting();
		}
		catch (BusCrashException e) {
			busConductor.fireBusCrashed(BusPhase.BUS_STARTING, e);
			busConductor.fireBusTerminated();
			
			throw e;
		}
		
		started = true;
	}
	
	@Override
	public void stopBus() throws BusCrashException {
		
		if (!started) {
			throw new IllegalStateException("Bus Not Started.");
		}
		
		if (tripping) {
			tripEnd();
		}
		
		try {
			busConductor.fireBusStopping();
		}
		finally {
			terminateBus(BusPhase.BUS_STOPPED);
		}
	}		
	
	@Override
	public boolean add(T bean) {
		
		if (!started) {
			throw new IllegalStateException("Bus Not Started.");
		}
		
		// if this is the first bean, start the trip.
		if (!tripping) {
			try {
				tripBegin();
			} catch (BusCrashException e) {
				crashBus(BusPhase.TRIP_BEGINNING, e);
				
				throw new RuntimeException(e);
			}
			
			// place here so we only log once.
			if (to == null) {
				logger.info("To is not set. All beans will be ignored.");
			}
		}
		
		try {
			if (to == null) {
				return false;
			}
			else {
				return to.add(bean);
			}
		}
		catch (RuntimeException e) {
			
			crashBus(BusPhase.BUS_RUNNING, e);
			
			throw e;
		}
		
	}
	
	private void tripBegin() throws BusCrashException {
		
		try {
			onTripBegin();
			
			tripping = true;
			
			busConductor.fireTripBeginning();			
		}
		catch (BusCrashException e) {
			crashBus(BusPhase.TRIP_BEGINNING, e);
			
			throw e;
		}
	}
	
	private void tripEnd() throws BusCrashException {
		
		try {
			busConductor.fireTripEnding();
			
			tripping = false;
			
			onTripEnd();
		}
		catch (BusCrashException e) {
			crashBus(BusPhase.TRIP_ENDING, e);
			
			throw e;
		}
	}
	
	private void crashBus(BusPhase phase, Exception e) {
	
		try {
			busConductor.fireBusCrashed(phase, e);
		}
		finally {
			terminateBus(BusPhase.BUS_CRASHED);
		}
	}
	
	private void terminateBus(BusPhase phase) {
		
		started = false;
		
		busConductor.fireBusTerminated();
	}
	
	protected void onTripBegin() {
		
	}
	
	protected void onTripEnd() {
		
	}
	
	
	protected void onBusCrash() {
		
	}
	
	public Collection<? super T> getTo() {
		return to;
	}

	public void setTo(Collection<? super T> to) {
		this.to = to;
	}

	public Runnable getStopBusCommand() {
		return stopBusCommand;
	}
	
	public BusConductor getBusConductor() {
		return busConductor;
	}
}
