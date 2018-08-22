package org.oddjob.scheduling;

import org.oddjob.jobs.GrabJob;

/**
 * Provides a guard that allows only one winning {@link Outcome} but
 * any number of loosing outcomes.
 * 
 * @author rob
 *
 * @see GrabJob
 */
public interface Keeper {

	/**
	 * Attempt to Grab the right to continue.
	 * 
	 * @param ourIdentifier Uniquely identifier the grabber. This will 
	 * something like the host name.
	 * @param instanceIdentifier Identify an instance of a grab. This
	 * will be something like the schedule date.
	 * 
	 * @return The outcome of the grab.
	 */
	public Outcome grab(String ourIdentifier, Object instanceIdentifier);
}
