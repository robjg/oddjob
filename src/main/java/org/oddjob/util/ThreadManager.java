/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.util;


/**
 * The thread manager keeps track of active threads. It can be used to
 * ensure that all threads are complete before a job terminates.
 */
public interface ThreadManager {

	/**
	 * Run a job with the default ClassLoader
	 * 
	 * @param runnable The job.
	 * @param description The description.
	 */
	void run(Runnable runnable, String description);
	
	/**
	 * Return a array of the descriptions of all active threads. 
	 * The description of the thread making the request is excluded. This
	 * is because method is used to see if a server can stop, so a server
	 * can run a job which stops itself.
	 * 
	 * @return A list of descriptions.
	 */
	String[] activeDescriptions();
		
	/**
	 * Close the ThreadManager and free resource. This may
	 * involve interrupting Threads or similar to ensure nothing is
	 * still running.
	 */
	void close();
	
}
