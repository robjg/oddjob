/*
 * Based heavily on the Log4j NDC published under the terms of the Apache Software License
 * version 1.1 
 */
package org.oddjob.logging;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.oddjob.util.Restore;
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
	private static InheritableThreadLocal<AtomicReference<LogContext>> local = 
			new InheritableThreadLocal<AtomicReference<LogContext>>() {
		protected AtomicReference<LogContext> initialValue() {
			return new AtomicReference<>();
		}
		protected AtomicReference<LogContext> childValue(AtomicReference<LogContext> parentValue) {
				return new AtomicReference<>(parentValue.get());
		}
	};
	
	// No instances allowed.
	private OddjobNDC() {
	}


	/**
	 * Looks at the last diagnostic context at the top of this NDC without
	 * removing it.
	 * 
	 * <p>
	 * The returned value is the value that was pushed last. If no context is
	 * available, then null is returned.
	 * 
	 * @return LoggerAndJob The inner most diagnostic context.
	 * 
	 */
	public static Optional<LogContext> current() {
		return Optional.ofNullable(local.get().get());
	}

	/**
	 * Push new diagnostic context information for the current thread.
	 * 
	 * @param loggerName
	 *            The new diagnostic context information.
	 * 
	 */
	public static Restore push(String loggerName, Object job) {
		if (loggerName == null) {
			throw new NullPointerException("Can't push null logger name.");
		}
		if (job == null) {
			throw new NullPointerException("Can't push null job.");
		}
		
		LogContext existingContext = local.get().get();
		String padding = Optional.ofNullable(existingContext)
				.map(lc -> lc.getPadding())
				.map(p -> p + " ")
				.orElse("");

		LogContext logContext = new LogContext(loggerName, job, padding);
		
		local.get().set(logContext);

		Restore ndcRestore = setLoggingNDC(logContext);
		
		return new Restore() {

			@Override
			public void close() {
				ndcRestore.close();
				local.get().set(existingContext);
			};
		};
	}

	public static Restore setLoggingNDC(LogContext ndcs) {
		
		String existingLoggerName = MDC.get(MDC_LOGGER);
		String existingJobName = MDC.get(MDC_JOB_NAME);
		String existingPadding = MDC.get(MDC_LEVEL_PADDING);
		
		MDC.put(MDC_LOGGER, ndcs.getLogger());
		MDC.put(MDC_JOB_NAME, String.valueOf(ndcs.getJob()));
		MDC.put(MDC_LEVEL_PADDING, ndcs.getPadding());
		
		return new Restore() {
			@Override
			public void close() {
				if (existingLoggerName == null) {
					MDC.remove(MDC_LOGGER);
				}
				else {
					MDC.put(MDC_LOGGER, existingLoggerName);
				}

				if (existingJobName == null) {
					MDC.remove(MDC_JOB_NAME);
				}
				else {
					MDC.put(MDC_JOB_NAME, existingJobName);
				}
				
				if (existingPadding == null) {
					MDC.remove(MDC_LEVEL_PADDING);
				}
				else {
					MDC.put(MDC_LEVEL_PADDING, existingPadding);
				}
			}
		};
	}
	
	/**
	 * Holds Logger and Job information for the Stack.
	 */
	public static class LogContext implements Cloneable {
		
		private final String logger;
		
		private final Object job;
		
		private final String padding;
		
		public LogContext(String logger, Object job, String padding) {
			this.logger = logger;
			this.job = job;
			this.padding = padding;
		}
		
		public Object getJob() {
			return job;
		}
		
		public String getLogger() {
			return logger;
		}
		
		public String getPadding() {
			return padding;
		}
		
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
	}
}
