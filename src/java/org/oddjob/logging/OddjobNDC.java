/*
 * Based heavily on the Log4j NDC published under the terms of the Apache Software License
 * version 1.1 
 */
package org.oddjob.logging;

import java.util.Stack;

import org.apache.log4j.MDC;
import org.oddjob.logging.log4j.Log4jArchiver;

/**
 * An OddjobNDC is a Nested Diagnostic Context for Oddjob. It provides a way of
 * allowing component by component logging.
 * <p>
 * It is very crude wrapper for the Log4j MDC class. One day it would be nice to work
 * out how to apply this to other logging utilities.
 * 
 */

public class OddjobNDC {
	
	private static InheritableThreadLocal local = new InheritableThreadLocal() {
		protected Object initialValue() {
			return new Stack();
		}
		protected Object childValue(Object parentValue) {
			if (parentValue != null) {
				return ((Stack) parentValue).clone();
			}
			else {
				return null;
			}
		}
	};
	
	// No instances allowed.
	private OddjobNDC() {
	}

	/**
	 * Clients should call this method before leaving a diagnostic context.
	 * 
	 * <p>
	 * The returned value is the value that was pushed last. If no context is
	 * available, then the empty string "" is returned.
	 * 
	 * @return String The innermost diagnostic context.
	 * 
	 */
	public static String pop() {
		Stack stack = (Stack) local.get();
		String result = (String) stack.pop();
		if (stack.isEmpty()) {
			MDC.remove(Log4jArchiver.MDC);
		} else {
			MDC.put(Log4jArchiver.MDC, stack.peek());
		}
		return result; 
	}

	/**
	 * Looks at the last diagnostic context at the top of this NDC without
	 * removing it.
	 * 
	 * <p>
	 * The returned value is the value that was pushed last. If no context is
	 * available, then the empty string "" is returned.
	 * 
	 * @return String The innermost diagnostic context.
	 * 
	 */
	public static String peek() {
		Stack stack = (Stack) local.get();		
		if (stack.isEmpty()) {
			return null;
		}
		return (String) stack.peek();
	}

	/**
	 * Push new diagnostic context information for the current thread.
	 * 
	 * @param loggerName
	 *            The new diagnostic context information.
	 * 
	 */
	public static void push(String loggerName) {
		if (loggerName == null) {
			throw new NullPointerException("Can't push null logger name.");
		}
		Stack stack = (Stack) local.get();
		stack.push(loggerName);
		MDC.put(Log4jArchiver.MDC, loggerName);
	}

}
