package org.oddjob.images;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.state.State;

/**
 * Maps of States to Icons.
 * 
 * @author rob
 *
 */
public class StateIcons {

	private static final Map<State, String> iconIds = 
		new ConcurrentHashMap<State, String>();
		
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
		iconIds.put(ParentState.STARTED, IconHelper.STARTED);
		iconIds.put(ParentState.INCOMPLETE, IconHelper.NOT_COMPLETE);
		iconIds.put(ParentState.COMPLETE, IconHelper.COMPLETE);
		iconIds.put(ParentState.EXCEPTION, IconHelper.EXCEPTION);
		iconIds.put(ParentState.DESTROYED, IconHelper.INVALID);
		
		iconIds.put(ServiceState.STARTABLE, IconHelper.STARTABLE);
		iconIds.put(ServiceState.STARTING, IconHelper.EXECUTING);
		iconIds.put(ServiceState.STARTED, IconHelper.STARTED);
		iconIds.put(ServiceState.STOPPED, IconHelper.STOPPED);
		iconIds.put(ServiceState.EXCEPTION, IconHelper.EXCEPTION);
		iconIds.put(ServiceState.DESTROYED, IconHelper.INVALID);		
	}
	
	public static void register(State state, String iconId) {

		if (iconId == null) {
			throw new NullPointerException("No Icon Id.");
		}
		
		iconIds.put(state, iconId);
	}
	
	/**
	 * Get the Icon for the given state.
	 * 
	 * @param state The State.
	 * @return An Icon, or the Null Icon if the state isn't mapped.
	 */
	public static String iconFor(State state) {

		String iconId = iconIds.get(state);
		
		if (iconId == null) {
			return IconHelper.NULL;
		}
		
		return iconId;
	}
}
