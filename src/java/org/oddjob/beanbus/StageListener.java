package org.oddjob.beanbus;

import java.util.EventListener;

/**
 * Something that listens to events from a {@link StageNotifier}.
 *  
 * @author rob
 *
 */
public interface StageListener extends EventListener {

	/**
	 * Stage starting.
	 * 
	 * @param event
	 */
	public void stageStarting(StageEvent event);
	
	/**
	 * Stage complete.
	 * 
	 * @param event
	 */
	public void stageComplete(StageEvent event);
}
