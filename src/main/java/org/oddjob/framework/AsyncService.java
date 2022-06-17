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
public interface AsyncService extends FallibleComponent {

	/**
	 * Start the service.
	 *
	 * @throws Exception If the service can't be started.
	 */
	void start() throws Exception;

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
