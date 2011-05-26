package org.oddjob.scheduling;


/**
 * The Outcome of attempting to grab something from a {@link Keeper}.
 * <p>
 * 
 * @author rob
 *
 */
public interface Outcome {

	/**
	 * Has this grab won.
	 * 
	 * @return
	 */
	public boolean isWon();
	
	/**
	 * The identifier of the winner
	 * 
	 * @return The identifier. Never null.
	 */
	public String getWinner();
	
}
