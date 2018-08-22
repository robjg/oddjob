package org.oddjob.framework;

import java.beans.ExceptionListener;

/**
 * A component that knows it might fail and would like to be able to inform
 * the framework when it does.
 * 
 * @author rob
 *
 */
public interface FallibleComponent {

	/**
	 * Accept an exception listener. The framework will use this method to 
	 * inject a listener into the component, that when notified of an
	 * exception, will inform the framework the component has entered an
	 * exception state.
	 * 
	 * @param exceptionListener
	 */
	public void acceptExceptionListener(ExceptionListener exceptionListener);
}
