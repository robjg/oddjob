package org.oddjob.framework;

/**
 * A service that starts asynchronously. It uses callbacks that interacts with
 * the framework to inform it when it has entered an exception state and
 * when it is started.
 * <p/>
 *
 * @author rob
 *
 */
public interface AsyncService extends FallibleComponent, Runnable {

	/**
	 * Accept a stop handle. The framework will use this method to
	 * inject a command, that when called, will inform
	 * the framework that the service has started.
	 * <p/>
	 *
	 * @param flagStarted
	 */
	void acceptCompletionHandle(Runnable flagStarted);
	
}
