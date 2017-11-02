package org.oddjob.jobs.tasks;

import org.junit.Test;

import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.tools.StateSteps;

import org.oddjob.OjTestCase;

public class JobTaskViewTest extends OjTestCase {

   @Test
	public void testJobStateExceptionReflectedOK() {
		
		FlagState flagState = new FlagState(JobState.EXCEPTION);
		
		JobTaskView test = new JobTaskView(flagState) {
			@Override
			protected Object onDone() {
				throw new RuntimeException("Unexpected.");
			}
		};
		
		StateSteps states = new StateSteps(test);
		states.startCheck(TaskState.PENDING, TaskState.INPROGRESS, TaskState.EXCEPTION);
		
		flagState.run();
		
		states.checkNow();
		
		states.startCheck(TaskState.EXCEPTION);
		
		flagState.hardReset();
		
		flagState.setState(JobState.COMPLETE);
		
		flagState.run();
		
		states.checkNow();
	}
}
