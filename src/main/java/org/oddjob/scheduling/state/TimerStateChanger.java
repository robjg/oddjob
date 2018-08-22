package org.oddjob.scheduling.state;

import org.oddjob.images.IconHelper;
import org.oddjob.persist.Persistable;
import org.oddjob.state.BaseStateChanger;
import org.oddjob.state.StateChanger;
import org.oddjob.state.StateHandler;

/**
 * A {@link StateChanger} for {@link TimerState}s.
 * 
 * @author rob
 *
 */
public class TimerStateChanger extends BaseStateChanger<TimerState> {
		
	public TimerStateChanger(StateHandler<TimerState> stateHandler,
			IconHelper iconHelper, Persistable persistable) {
		super(stateHandler, iconHelper, persistable, TimerState.EXCEPTION);
	}
	
}
