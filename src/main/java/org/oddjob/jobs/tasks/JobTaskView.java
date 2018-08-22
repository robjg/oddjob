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
			
			// Called before the event is fired so the response will
			// be known by clients waiting for the complete event.
			if (StateConditions.DONE.test(state)) {
				response = onDone();
			}
			
			if (StateConditions.FINISHED.test(state)) {
				event.getSource().removeStateListener(this);
			}
			
			stateHandler.callLocked(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					
					TaskState newState;
					Throwable exception = null;
					
					if (state.isDestroyed()) {
						newState = TaskState.EXCEPTION;
						exception = new TaskException("Job Executing Task has been destroyed.");
					}
					else if (state.isStoppable()) {
						newState = TaskState.INPROGRESS;
					}
					else if (state.isIncomplete()) {
						newState = TaskState.INCOMPLETE;
					}
					else if (state.isComplete()) {
						newState = TaskState.COMPLETE;
					}
					else if (state.isException()) {
						newState = TaskState.EXCEPTION;
						exception = event.getException();
					}
					else if (state.isReady()) {
						newState = TaskState.PENDING;
					}
					else {
						throw new IllegalStateException("Unconvertable state " + state);
					}
					
					if (newState == stateHandler.lastStateEvent().getState()) {
						return null;
					}
					
					if (newState == TaskState.EXCEPTION) {
						stateHandler.setStateException(
								TaskState.EXCEPTION,
								exception, time);
					}
					else {
						stateHandler.setState(newState, time);
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
