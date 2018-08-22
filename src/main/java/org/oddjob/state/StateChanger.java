package org.oddjob.state;

import java.util.Date;

import org.oddjob.framework.JobDestroyedException;

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
	public void setState(S state) throws JobDestroyedException;
	
	/**
	 * Set the state to the given state with the
	 * given event time.
	 * 
	 * @param state
	 * @param date
	 */
	public void setState(S state, Date date) throws JobDestroyedException;

	/**
	 * Set the state to an EXCEPTION state.
	 * 
	 * @param t The Exception.
	 */
	public void setStateException(Throwable t) throws JobDestroyedException;
	
	/**
	 * Set the state to an EXCEPTION state with
	 * the given event time.
	 * 
	 * @param t The Exception.
	 */
	public void setStateException(Throwable t, Date date) throws JobDestroyedException;
}
