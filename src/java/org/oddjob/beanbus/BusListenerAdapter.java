package org.oddjob.beanbus;

/**
 * Unimplemented methods for {@link BusListener}.
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
	public void busCrashed(BusEvent event) {
	}
	
	@Override
	public void busTerminated(BusEvent event) {
	}
}
