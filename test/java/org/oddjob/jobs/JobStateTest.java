/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.jobs;

import junit.framework.TestCase;

import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

/**
 *
 * @author Rob Gordon.
 */
public class JobStateTest extends TestCase {

	JobState jobState;
	String iconId;
	
	JobStateListener stateListener = new JobStateListener() {
		public void jobStateChange(JobStateEvent event) {
			JobStateTest.this.jobState = event.getJobState();
		}
	};
	
	IconListener iconListener = new IconListener() {
		public void iconEvent(IconEvent event) {
			JobStateTest.this.iconId = event.getIconId();
		}
	};
		
	public void testException() {
		final FlagState j = new FlagState ();
		j.setName("Exception Test");
		j.setState(JobState.EXCEPTION);
		
		j.addJobStateListener(stateListener);
		j.addIconListener(iconListener);
		assertTrue("job state not READY.",
				jobState == JobState.READY);
		
		j.run();

		assertTrue("job state not EXCEPTION.",
				jobState == JobState.EXCEPTION);
		assertTrue("Icon not EXCEPTION",
				iconId.equals(IconHelper.EXCEPTION));
	}
	
	public void testNotComplete() {
		final FlagState j = new FlagState ();
		j.setName("Test Not Complete");
		j.setState(JobState.INCOMPLETE);
		
		j.addJobStateListener(stateListener);
		j.addIconListener(iconListener);
		assertTrue("job state not READY.",
				jobState == JobState.READY);
		j.run();

		assertTrue("job state not NOT COMPLETE.",
				jobState == JobState.INCOMPLETE);
		assertTrue("Icon not NOT COMPLETE",
				iconId.equals(IconHelper.NOT_COMPLETE));
	}
	
	public void testComplete() {
		final FlagState j = new FlagState ();
		j.setName("Test Complete");
		j.setState(JobState.COMPLETE);
		
		j.addJobStateListener(stateListener);
		j.addIconListener(iconListener);
		assertTrue("job state not READY.",
				jobState == JobState.READY);
		
		j.run();

		assertTrue("job state not COMPLETE.",
				jobState == JobState.COMPLETE);
		assertTrue("Icon not COMPLETE",
				iconId.equals(IconHelper.COMPLETE));
	}
}
