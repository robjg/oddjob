package org.oddjob.jobs.tasks;

import java.util.Date;

import org.oddjob.state.State;
import org.oddjob.state.StateChanger;
import org.oddjob.state.StateEvent;

/**
 * Fire appropriate Task states.
 * 
 * @author rob
 *
 */
public class TaskStateChanger {
	
	private final StateChanger<TaskState> stateChanger;
	
	public TaskStateChanger(StateChanger<TaskState> stateChanger) {
		this.stateChanger = stateChanger;
	}
	
	public void propergate(StateEvent event) {
		
		State state = event.getState();
		Date time = event.getTime();
		
		if (state.isDestroyed()) {
			stateChanger.setStateException(
					new TaskException("Job Executing Task has been destroyed."),
					time);
		}
		else if (state.isStoppable()) {
			stateChanger.setState(TaskState.INPROGRESS,time);
		}
		else if (state.isIncomplete()) {
			stateChanger.setState(TaskState.INCOMPLETE, time);
		}
		else if (state.isComplete()) {
			stateChanger.setState(TaskState.COMPLETE, time);
		}
		else if (state.isException()) {
			stateChanger.setStateException(event.getException(), time);
		}
		else if (state.isReady()) {
			stateChanger.setState(TaskState.PENDING, time);
		}
		else {
			throw new IllegalStateException("Unconvertable state " + state);
		}
	}
}
