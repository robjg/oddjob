package org.oddjob.images;

import org.oddjob.state.JobState;

public class StateIcons {

	private static final String[] icons = 
	{ 
		IconHelper.READY, 
		IconHelper.EXECUTING,
		IconHelper.NOT_COMPLETE,
		IconHelper.COMPLETE,
		IconHelper.EXCEPTION,
		IconHelper.NULL // destroyed
	};
	
	public static String iconFor(JobState jobState) {
		return icons[jobState.ordinal()];
	}
}
