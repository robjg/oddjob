package org.oddjob.events.state;

import org.oddjob.images.IconHelper;
import org.oddjob.persist.Persistable;
import org.oddjob.state.BaseStateChanger;
import org.oddjob.state.StateChanger;
import org.oddjob.state.StateHandler;

/**
 * A {@link StateChanger} for {@link EventState}s.
 * 
 * @author rob
 *
 */
public class EventStateChanger extends BaseStateChanger<EventState> {
		
	public EventStateChanger(StateHandler<EventState> stateHandler,
			IconHelper iconHelper, Persistable persistable) {
		super(stateHandler, iconHelper, persistable, EventState.EXCEPTION);
	}
	
}
