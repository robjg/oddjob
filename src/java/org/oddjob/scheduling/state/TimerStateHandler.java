package org.oddjob.scheduling.state;

import org.oddjob.Stateful;
import org.oddjob.state.StateHandler;


/**
 * Helps timers handle state change.
 * 
 * @author Rob Gordon
 */
public class TimerStateHandler 
extends StateHandler<TimerState> {
	
	/**
	 * Constructor.
	 * 
	 * @param source The source for events.
	 */
	public TimerStateHandler(Stateful source) {
		super(source, TimerState.READY);
	}	
	
}

