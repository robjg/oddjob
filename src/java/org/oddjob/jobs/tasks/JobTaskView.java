package org.oddjob.jobs.tasks;

import java.util.Date;
import java.util.concurrent.Callable;

import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.state.State;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateHandler;
import org.oddjob.state.StateListener;

/**
 * A {@link TaskView} that is based on a job that is performing the task.
 * 
 * @author rob
 *
 */
abstract public class JobTaskView implements TaskView {

	private final StateHandler<TaskState> stateHandler = 
			new StateHandler<TaskState>(this, TaskState.PENDING);

	private volatile Object response;
	
	private final StateListener listener = new StateListener() {
		
		public void jobStateChange(final StateEvent event) {
			
			final State state = event.getState();
			final Date time = event.getTime();
			
			if (StateConditions.DONE.test(state)) {
				response = onDone();
			}
			
			stateHandler.callLocked(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					
					if (state.isDestroyed()) {
						stateHandler.setStateException(
								TaskState.EXCEPTION,
								new TaskException("Job Executing Task has been destroyed."),
								time);
					}
					else if (state.isStoppable()) {
						stateHandler.setState(TaskState.INPROGRESS,time);
					}
					else if (state.isIncomplete()) {
						stateHandler.setState(TaskState.INCOMPLETE, time);
					}
					else if (state.isComplete()) {
						stateHandler.setState(TaskState.COMPLETE, time);
					}
					else if (state.isException()) {
						stateHandler.setStateException(
								TaskState.EXCEPTION,
								event.getException(), time);
					}
					else if (state.isReady()) {
						stateHandler.setState(TaskState.PENDING, time);
					}
					else {
						throw new IllegalStateException("Unconvertable state " + state);
					}
					
					stateHandler.fireEvent();
					
					return null;
				}
			});
		}
	};
	
	
	public JobTaskView(Stateful job) {
		job.addStateListener(listener);
	}
	
	protected abstract Object onDone();
	
	@Override
	public StateEvent lastStateEvent() {
		return stateHandler.lastStateEvent();
	}
	
	@Override
	public void addStateListener(StateListener listener)
			throws JobDestroyedException {
		stateHandler.addStateListener(listener);
	}
	
	@Override
	public void removeStateListener(StateListener listener) {
		stateHandler.removeStateListener(listener);
	}
	
	@Override
	public Object getTaskResponse() {
		return response;
	}
}
