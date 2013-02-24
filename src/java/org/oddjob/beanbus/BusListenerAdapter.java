package org.oddjob.beanbus;

/**
 * Provide empty implementations of all {@link BusListener} methods
 * to make implementing bus listeners easier.
 * 
 * @author rob
 *
 */
public class BusListenerAdapter implements BusListener {

	@Override
	public void busStarting(BusEvent event) throws BusCrashException {
	}
	
	@Override
	public void tripBeginning(BusEvent event) throws BusCrashException {
	}
	
	@Override
	public void tripEnding(BusEvent event) throws BusCrashException {
	}
	
	@Override
	public void busStopRequested(BusEvent event) {
	}
	
	@Override
	public void busStopping(BusEvent event) throws BusCrashException {
	}
	
	@Override
	public void busCrashed(BusEvent event, BusException e) {
	}
	
	@Override
	public void busTerminated(BusEvent event) {
	}
}
