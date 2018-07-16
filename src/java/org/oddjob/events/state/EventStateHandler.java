package org.oddjob.events.state;

import org.oddjob.Stateful;
import org.oddjob.state.StateHandler;


/**
 * Helps timers handle state change.
 * 
 * @author Rob Gordon
 */
public class EventStateHandler 
extends StateHandler<EventState> {
	
	/**
	 * Constructor.
	 * 
	 * @param source The source for events.
	 */
	public EventStateHandler(Stateful source) {
		super(source, EventState.READY);
	}	
	
}

