package org.oddjob.state;

import org.oddjob.Stateful;


/**
 * Helps Structurals handle state change.
 * 
 * @author Rob Gordon
 */
public class ParentStateHandler 
extends StateHandler<ParentState> {
	
	/**
	 * Constructor.
	 * 
	 * @param source The source for events.
	 */
	public ParentStateHandler(Stateful source) {
		super(source, ParentState.READY);
	}	
	
}

