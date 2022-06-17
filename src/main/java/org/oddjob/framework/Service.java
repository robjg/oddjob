package org.oddjob.framework;

import org.oddjob.Stoppable;

/**
 * Definition of a Service. A service is different to a job in that it is not intended to complete
 * without being stopped.
 *
 * @see org.oddjob.state.ServiceState
 */
public interface Service extends Stoppable {

	/**
	 * Start the service.
	 *
	 * @throws Exception If the service can't be started.
	 */
	void start() throws Exception;
	
}
