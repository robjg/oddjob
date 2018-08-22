package org.oddjob.state;

import org.oddjob.images.IconHelper;
import org.oddjob.persist.Persistable;

/**
 * A {@link StateChanger} for {@link ParentState}s.
 * 
 * @author rob
 *
 */
public class ParentStateChanger extends BaseStateChanger<ParentState> {
		
	public ParentStateChanger(StateHandler<ParentState> stateHandler,
			IconHelper iconHelper, Persistable persistable) {
		super(stateHandler, iconHelper, persistable, ParentState.EXCEPTION);
	}
	
}
