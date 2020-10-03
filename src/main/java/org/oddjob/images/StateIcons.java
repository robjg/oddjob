package org.oddjob.images;

import org.oddjob.state.State;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps of States to Icons.
 * 
 * @author rob
 *
 */
public class StateIcons {

	private static final Map<State, String> iconIds =
			new ConcurrentHashMap<>();
		
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
