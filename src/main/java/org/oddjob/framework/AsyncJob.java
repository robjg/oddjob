package org.oddjob.framework;

import java.util.function.IntConsumer;


/**
 * A Job that completes asynchronously. It uses callbacks that interacts with
 * the framework to inform it when it has entered an exception state and
 * when it is complete. Unlike a {@link Service} an Async job is expected
 * to COMPLETE without being stopped and may also terminate as INCOMPLETE.
 * <p/>
 * Async jobs should be short lived or {@link org.oddjob.Stoppable}.
 *
 * @author rob
 *
 */
public interface AsyncJob extends FallibleComponent {

	/**
	 * Accept a stop handle. The framework will use this method to
	 * inject a consumer into the component, that when called, will inform
	 * the framework that the component has stopped.
	 * <p/>
	 * The consumer should be called with 0 to signal complete and anything else
	 * to signal incomplete.
	 *
	 * @param stopWithState
	 */
	void acceptCompletionHandle(IntConsumer stopWithState);
	
}
