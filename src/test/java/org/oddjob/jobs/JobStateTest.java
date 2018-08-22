/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.jobs;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

/**
 *
 * @author Rob Gordon.
 */
public class JobStateTest extends OjTestCase {

	JobState jobState;
	String iconId;
	
	StateListener stateListener = new StateListener() {
		public void jobStateChange(StateEvent event) {
			JobStateTest.this.jobState = (JobState) event.getState();
		}
	};
	
	IconListener iconListener = new IconListener() {
		public void iconEvent(IconEvent event) {
			JobStateTest.this.iconId = event.getIconId();
		}
	};
		
   @Test
	public void testException() {
		final FlagState j = new FlagState ();
		j.setName("Exception Test");
		j.setState(JobState.EXCEPTION);
		
		j.addStateListener(stateListener);
		j.addIconListener(iconListener);
		assertTrue("job state not READY.",
				jobState == JobState.READY);
		
		j.run();

		assertTrue("job state not EXCEPTION.",
				jobState == JobState.EXCEPTION);
		assertTrue("Icon not EXCEPTION",
				iconId.equals(IconHelper.EXCEPTION));
	}
	
   @Test
	public void testNotComplete() {
		final FlagState j = new FlagState ();
		j.setName("Test Not Complete");
		j.setState(JobState.INCOMPLETE);
		
		j.addStateListener(stateListener);
		j.addIconListener(iconListener);
		assertTrue("job state not READY.",
				jobState == JobState.READY);
		j.run();

		assertTrue("job state not NOT COMPLETE.",
				jobState == JobState.INCOMPLETE);
		assertTrue("Icon not NOT COMPLETE",
				iconId.equals(IconHelper.NOT_COMPLETE));
	}
	
   @Test
	public void testComplete() {
		final FlagState j = new FlagState ();
		j.setName("Test Complete");
		j.setState(JobState.COMPLETE);
		
		j.addStateListener(stateListener);
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
