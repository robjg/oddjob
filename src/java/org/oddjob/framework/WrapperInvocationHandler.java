package org.oddjob.framework;

import java.lang.reflect.InvocationHandler;

/**
 * The {@code InvocationHandler} used by an Proxy for a Runnable or
 * other wrapped.
 * 
 * @author rob
 *
 */
public interface WrapperInvocationHandler extends InvocationHandler {

	/**
	 * Get the wrapped component.
	 * 
	 * @return Must not be null.
	 */
	public Object getWrappedComponent();
}
