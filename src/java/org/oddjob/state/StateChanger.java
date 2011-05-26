package org.oddjob.state;

import java.util.Date;

/**
 * Implementations provide the ability to change something's
 * {@link JobState}.
 * 
 * @author rob
 *
 */
public interface StateChanger {

	/**
	 * Set the state to given state.
	 * 
	 * @param state
	 */
	public void setJobState(JobState state);
	
	/**
	 * Set the state to the given state with the
	 * given event time.
	 * 
	 * @param state
	 * @param date
	 */
	public void setJobState(JobState state, Date date);

	/**
	 * Set the state to {@link JobState#EXCEPTION} with
	 * 
	 * @param t The Exception.
	 */
	public void setJobStateException(Throwable t);
	
	/**
	 * Set the state to {@link JobState#EXCEPTION} with
	 * the given event time.
	 * 
	 * @param t The Exception.
	 */
	public void setJobStateException(Throwable t, Date date);
}
