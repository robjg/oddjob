/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.util;

import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * The thread manager keeps track of active threads. It can be used to
 * ensure that all threads are complete before a job terminates.
 */
public class SimpleThreadManager implements ThreadManager {
	private static final Logger logger = LoggerFactory.getLogger(SimpleThreadManager.class);
	
	/** Map of active threads to their description. */
	private final Map<Runnable, Remember> active =
			new HashMap<>();

	private final Executor executor;

	private final Runnable stop;

	/**
	 * Default Constructor. Uses a default ExecutorService.
	 */
	public SimpleThreadManager() {
		this(null);
	}
	
	/**
	 * Constructor uses provided ExecutorService.
	 * 
	 * @param executor
	 */
	public SimpleThreadManager(Executor executor) {
		if (executor == null) {
			ExecutorService executorService = Executors.newCachedThreadPool();
			this.executor = executorService;
			this.stop = executorService::shutdownNow;
		}
		else {
			this.executor = executor;
			this.stop = () -> {};
		}
	}
	
	/**
	 * Run a job.
	 * 
	 * @param runnable The job.
	 * @param description The description of the job.
	 */
	public void run(final Runnable runnable, final String description) {
		Runnable wrapper  = new Runnable() {
			public void run() {
				try {
					runnable.run();
				}
				catch (Throwable t) {
					logger.error("Failed running [" + description + "]", t);
				}
				finally {
					synchronized (active) {
						active.remove(runnable);
					}
				}
			}
			@Override
			public String toString() {
				return "Runnable for " + description;
			}
		};
		
		synchronized (active) {
			CompletableFuture<?> future = CompletableFuture.runAsync(wrapper, executor);
			active.put(runnable, new Remember(description, future));
		}
	}
	
	/**
	 * Return a array of the descriptions of all active threads. 
	 * The description of the thread making the request is excluded. This
	 * is because method is used to see if a server can stop, so a server
	 * can run a job which stops itself.
	 * 
	 * @return A list of descriptions.
	 */
	public String[] activeDescriptions() {
		List<String> results = new ArrayList<>();
		synchronized (active) {
			for (Remember remember : active.values() ) {
				results.add( remember.description );
			}
			return results.toArray(new String[0]);
		}
	}
	
	public String toString() {
		return "ThreadManager: " + active.size() + " active threads.";
	}
	
	public void close() {
		synchronized (active) {
			for (Map.Entry<Runnable, Remember>  entry : active.entrySet()) {
				Runnable runnable = entry.getKey();
				Remember remember = entry.getValue();
				if (runnable instanceof Stoppable) {
					try {
						((Stoppable) runnable).stop();
					} catch (FailedToStopException e) {
						logger.warn("Failed to stop [" + runnable + "]", e);
					}
				}
				else {
					remember.future.cancel(true);
				}
			}
		}
		stop.run();
	}
	
	static class Remember {
		private final String description;
		private final Future<?> future;
		
		Remember(String description, Future<?> runnable) {
			this.description = description;
			this.future = runnable;
		}
	}
}
