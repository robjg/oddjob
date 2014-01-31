package org.oddjob.framework;

import org.oddjob.Stoppable;


/**
 * A Component that is probably a {@link Service} that interacts with
 * the framework to inform it when it has entered an exception state and
 * when it is complete.
 * 
 * @author rob
 *
 */
public interface AsynchronousJob extends FallibleComponent, Stoppable {

	/**
	 * Accept a stop handle. The framework will use this method to
	 * inject a command into the component, that when run, will inform
	 * the framework that the component has stopped.
	 * 
	 * @param runnable
	 */
	public void acceptStopHandle(Runnable runnable);
	
}
