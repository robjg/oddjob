/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.state;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.SerializableJob;

/**
 * @oddjob.description
 * 
 * When run it's state becomes the given state.
 * 
 * 
 * @author Rob Gordon
 */
public class FlagState extends SerializableJob {
	private static final long serialVersionUID = 2009032000L;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The state to flag.
	 * @oddjob.required No, defaults to COMPLETE.
	 */
	private JobState state;
	
	public FlagState() {
		state = JobState.COMPLETE;
	}
	
	public FlagState(JobState state) {
		this.state = state;
	}
	
	protected int execute() throws Exception {
		if (state.equals(JobState.COMPLETE)) {
			return 0;
		}
		if (state.equals(JobState.INCOMPLETE)) {
			return 1;
		}
		throw new Exception("Flagged Exception.");
	}

	/**
	 * @return Returns the desired.
	 */
	public JobState getState() {
		return state;
	}
	
	/**
	 * @param desired The desired to set.
	 */
	@ArooaAttribute
	public void setState(JobState desired) {
		this.state = desired;
	}
}
