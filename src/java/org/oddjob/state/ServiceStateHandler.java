package org.oddjob.state;

import org.oddjob.Stateful;


/**
 * Helps Services handle state change.
 * 
 * @author Rob Gordon
 */
public class ServiceStateHandler 
extends StateHandler<ServiceState> {
	
	/**
	 * Constructor.
	 * 
	 * @param source The source for events.
	 */
	public ServiceStateHandler(Stateful source) {
		super(source, ServiceState.READY);
	}	
	
}

