package org.oddjob.beanbus;

/**
 * Notify listeners of stages. I want to name this <code>Conductor</code>
 * but I don't know if that's not taking the bus metaphor too far.
 * 
 * @author rob
 *
 */
public interface StageNotifier {

	/**
	 * Add a listener.
	 * 
	 * @param listener The listener.
	 */
	public void addStageListener(StageListener listener);
	
	/**
	 * Remove a listener.
	 * 
	 * @param listener The listener.
	 */
	public void removeStageListener(StageListener listener);
	
}
