package org.oddjob.beanbus;

/**
 * Provide empty implementations of all {@link BusListener} methods
 * to make implementing bus listeners easier.
 * <p>
 * This allows BusConductor to be tracked because it will be set each
 * time a bus part is configured.
 * 
 * @author rob
 *
 */
public class TrackingBusListener {

	private BusConductor busConductor;

	private final BusListener listener = new BusListener() {
		
		@Override
		public void busStarting(BusEvent event) throws BusCrashException {
			TrackingBusListener.this.busStarting(event);
		}
		
		@Override
		public void tripBeginning(BusEvent event) throws BusCrashException {
			TrackingBusListener.this.tripBeginning(event);
		}
		
		@Override
		public void tripEnding(BusEvent event) throws BusCrashException {
			TrackingBusListener.this.tripEnding(event);
		}
		
		@Override
		public void busStopping(BusEvent event) throws BusCrashException {
			TrackingBusListener.this.busStopping(event);
		}
		
		@Override
		public void busStopRequested(BusEvent event) {
			TrackingBusListener.this.busStopRequested(event);
		}
		
		@Override
		public void busCrashed(BusEvent event) {
			TrackingBusListener.this.busCrashed(event);
		}
		
		@Override
		public void busTerminated(BusEvent event) {
			TrackingBusListener.this.busTerminated(event);
		}
	};
	
	public void setBusConductor(BusConductor busConductor) {
		if (this.busConductor == busConductor) {
			return;
		}
		if (this.busConductor != null) {
			this.busConductor.removeBusListener(listener);
		}
		if (busConductor != null) {
			busConductor.addBusListener(listener);
		}
		this.busConductor = busConductor;
	}
	
	public void busStarting(BusEvent event) throws BusCrashException {
	}
	
	public void tripBeginning(BusEvent event) throws BusCrashException {
	}
	
	public void tripEnding(BusEvent event) throws BusCrashException {
	}
	
	public void busStopRequested(BusEvent event) {
	}
	
	public void busStopping(BusEvent event) throws BusCrashException {
	}
	
	public void busCrashed(BusEvent event) {
	}
	
	public void busTerminated(BusEvent event) {
	}
}
