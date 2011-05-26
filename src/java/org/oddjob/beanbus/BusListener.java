package org.oddjob.beanbus;

import java.util.EventListener;

public interface BusListener extends EventListener {

	void busStarting(BusEvent event) throws CrashBusException;
		
	void busStopping(BusEvent event) throws CrashBusException;
	
	void busTerminated(BusEvent event);
	
	void busCrashed(BusEvent event, BusException e);
}
