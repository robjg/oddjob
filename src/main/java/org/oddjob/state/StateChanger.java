package org.oddjob.state;

import org.oddjob.framework.JobDestroyedException;

import java.time.Instant;

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
	 * @param state The state.
	 */
	void setState(S state) throws JobDestroyedException;
	
	/**
	 * Set the state to the given state with the
	 * given event time.
	 * Deprecated - use {@link #setState(State, Instant)}.
	 * 
	 * @param state The state.
	 * @param date The date.
	 */
//	@Deprecated(since="1.7", forRemoval=true)
//	default void setState(S state, Date date) throws JobDestroyedException {
//		setState(state, date.toInstant());
//	}

	/**
	 * Set the state to the given state with the
	 * given event time.
	 *
	 * @param state The state.
	 * @param stateInstant The unique event time.
	 */
	void setState(S state, StateInstant stateInstant) throws JobDestroyedException;

	/**
	 * Set the state to an EXCEPTION state.
	 * 
	 * @param t The Exception.
	 */
	void setStateException(Throwable t) throws JobDestroyedException;
	
	/**
	 * Set the state to an EXCEPTION state with
	 * the given event time.
	 * Deprecated - use {@link #setStateException(Throwable, Instant)} instead.
	 * 
	 * @param t The Exception.
	 * @param date The event time.
	 */
//	@Deprecated(since="1.7", forRemoval=true)
//	default void setStateException(Throwable t, Date date) throws JobDestroyedException {
//		setStateException(t, date.toInstant());
//	}

	/**
	 * Set the state to an EXCEPTION state with
	 * the given event time.
	 *
	 * @param t The Exception.
	 * @param stateInstant The unique event time.
	 */
	void setStateException(Throwable t, StateInstant stateInstant) throws JobDestroyedException;


}
