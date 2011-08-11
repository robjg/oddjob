package org.oddjob.images;

import java.util.HashMap;
import java.util.Map;

import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.state.State;

public class StateIcons {

	private static final Map<State, String> iconIds = 
		new HashMap<State, String>();
		
	static {

		iconIds.put(JobState.READY, IconHelper.READY);
		iconIds.put(JobState.EXECUTING, IconHelper.EXECUTING);
		iconIds.put(JobState.INCOMPLETE, IconHelper.NOT_COMPLETE);
		iconIds.put(JobState.COMPLETE, IconHelper.COMPLETE);
		iconIds.put(JobState.EXCEPTION, IconHelper.EXCEPTION);
		iconIds.put(JobState.DESTROYED, IconHelper.INVALID);
		
		iconIds.put(ParentState.READY, IconHelper.READY);
		iconIds.put(ParentState.EXECUTING, IconHelper.EXECUTING);
		iconIds.put(ParentState.ACTIVE, IconHelper.ACTIVE);
		iconIds.put(ParentState.INCOMPLETE, IconHelper.NOT_COMPLETE);
		iconIds.put(ParentState.COMPLETE, IconHelper.COMPLETE);
		iconIds.put(ParentState.EXCEPTION, IconHelper.EXCEPTION);
		iconIds.put(ParentState.DESTROYED, IconHelper.INVALID);
		
		iconIds.put(ServiceState.READY, IconHelper.READY);
		iconIds.put(ServiceState.STARTING, IconHelper.EXECUTING);
		iconIds.put(ServiceState.STARTED, IconHelper.STARTED);
		iconIds.put(ServiceState.INCOMPLETE, IconHelper.NOT_COMPLETE);
		iconIds.put(ServiceState.COMPLETE, IconHelper.COMPLETE);
		iconIds.put(ServiceState.EXCEPTION, IconHelper.EXCEPTION);
		iconIds.put(ServiceState.DESTROYED, IconHelper.INVALID);		
	}
	
	
	public static String iconFor(State state) {

		String iconId = iconIds.get(state);
		
		if (iconId == null) {
			return IconHelper.NULL;
		}
		
		return iconId;
	}
}
