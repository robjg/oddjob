package org.oddjob.framework;

import java.util.function.IntConsumer;


/**
 * A Component that is probably a {@link Service} that interacts with
 * the framework to inform it when it has entered an exception state and
 * when it is complete.
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
