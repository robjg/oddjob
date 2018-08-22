package org.oddjob.scheduling;

/**
 * An {@link Outcome} for the winner. The winner should notify when work
 * is complete. Notifying incomplete, or exception state is not
 * yet supported.
 * 
 * @author rob
 *
 */
public interface WinningOutcome extends Outcome {

	/**
	 * Used by the winner to update state to complete.
	 */
	public void complete();
	
}
