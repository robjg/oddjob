package org.oddjob.state;

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
	public void setState(S state);
	
	/**
	 * Set the state to the given state with the
	 * given event time.
	 * 
	 * @param state
	 * @param date
	 */
	public void setState(S state, Date date);

	/**
	 * Set the state to an EXCEPTION state.
	 * 
	 * @param t The Exception.
	 */
	public void setStateException(Throwable t);
	
	/**
	 * Set the state to an EXCEPTION state with
	 * the given event time.
	 * 
	 * @param t The Exception.
	 */
	public void setStateException(Throwable t, Date date);
}
