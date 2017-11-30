/*
 * Based heavily on the Log4j NDC published under the terms of the Apache Software License
 * version 1.1 
 */
package org.oddjob.logging;

import java.util.Stack;

import org.slf4j.MDC;

/**
 * An OddjobNDC is a Nested Diagnostic Context for Oddjob. It provides a way of
 * allowing component by component logging.
 * <p>
 * It is very crude wrapper for the Log4j MDC class. One day it would be nice to work
 * out how to apply this to other logging utilities.
 * <p>
 * Note that this is a thread based nested diagnostic context that does not 
 * interfere with Log4j's own. It populates log4j's mapped diagnostic context instead.
 * 
 * @author rob
 */
public class OddjobNDC implements LoggingConstants {
	
	/** The stack of contexts. */
	private static InheritableThreadLocal<Stack<LoggerAndJob>> local = 
			new InheritableThreadLocal<Stack<LoggerAndJob>>() {
		protected Stack<LoggerAndJob> initialValue() {
			return new Stack<LoggerAndJob>();
		}
		@SuppressWarnings("unchecked")
		protected Stack<LoggerAndJob> childValue(Stack<LoggerAndJob> parentValue) {
			if (parentValue != null) {
				return (Stack<LoggerAndJob>)parentValue.clone();
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
	 * @return LoggerAndJob The innermost diagnostic context.
	 * 
	 */
	public static LoggerAndJob pop() {
		Stack<LoggerAndJob> stack = local.get();
		LoggerAndJob result = stack.pop();
		if (stack.isEmpty()) {
			MDC.remove(MDC_LOGGER);
			MDC.remove(MDC_JOB_NAME);
		} else {
			LoggerAndJob peek = stack.peek();
			
			MDC.put(MDC_LOGGER, peek.getLogger());
			MDC.put(MDC_JOB_NAME, peek.getJob().toString());
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
	 * @return LoggerAndJob The innermost diagnostic context.
	 * 
	 */
	public static LoggerAndJob peek() {
		Stack<LoggerAndJob> stack = local.get();		
		if (stack.isEmpty()) {
			return null;
		}
		return stack.peek();
	}

	/**
	 * Push new diagnostic context information for the current thread.
	 * 
	 * @param loggerName
	 *            The new diagnostic context information.
	 * 
	 */
	public static void push(String loggerName, Object job) {
		if (loggerName == null) {
			throw new NullPointerException("Can't push null logger name.");
		}
		if (job == null) {
			throw new NullPointerException("Can't push null job.");
		}
		
		Stack<LoggerAndJob> stack = local.get();
		stack.push(new LoggerAndJob(loggerName, job));
		
		MDC.put(MDC_LOGGER, loggerName);
		MDC.put(MDC_JOB_NAME, job.toString());
	}

	/**
	 * Holds Logger and Job information for the Stack.
	 */
	public static class LoggerAndJob implements Cloneable {
		
		private final String logger;
		
		private final Object job;
		
		public LoggerAndJob(String logger, Object job) {
			this.logger = logger;
			this.job = job;
		}
		
		public Object getJob() {
			return job;
		}
		
		public String getLogger() {
			return logger;
		}
		
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
	}
}
