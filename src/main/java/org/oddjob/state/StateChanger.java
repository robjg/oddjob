package org.oddjob.state;

import org.oddjob.framework.JobDestroyedException;

import java.util.Date;

/**
 * Implementations provide the ability to change something's
 * {@link JobState}.
 * 
 * @author rob
 *
 */
public interface StateChanger<S extends State> {

	/**
	 * Set the state to given state.
	 * 
	 * @param state
	 */
	void setState(S state) throws JobDestroyedException;
	
	/**
	 * Set the state to the given state with the
	 * given event time.
	 * 
	 * @param state
	 * @param date
	 */
	void setState(S state, Date date) throws JobDestroyedException;

	/**
	 * Set the state to an EXCEPTION state.
	 * 
	 * @param t The Exception.
	 */
	void setStateException(Throwable t) throws JobDestroyedException;
	
	/**
	 * Set the state to an EXCEPTION state with
	 * the given event time.
	 * 
	 * @param t The Exception.
	 */
	void setStateException(Throwable t, Date date) throws JobDestroyedException;
}
