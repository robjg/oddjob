package org.oddjob.state;

import java.util.ArrayList;
import java.util.List;

import org.oddjob.Stateful;
import org.oddjob.Structural;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

/**
 * Track, and aggregate the states of child jobs. Aggregation is
 * achieved using the given {@link StateOperator}.
 * 
 * @author rob
 *
 */
public class StructuralStateHelper implements Stateful {

	/** Used to capture state into. */
	private final JobStateHandler stateHandler = 
		new JobStateHandler(this);
	
	/** The states of the children. */
	private final List<StateHolder> states = 
		new ArrayList<StateHolder>();
	
	/** The listeners listening to the children. */
	private final List<JobStateListener> listeners = 
		new ArrayList<JobStateListener>();

	/** The {@link StateOperator}. */
	private final StateOperator operator;

	/** Holder is required because the position of the children
	 * can move. The state listener uses a holder instead
	 * of an index to insert state.
	 */
	class StateHolder {
		private JobState state = JobState.COMPLETE;
	}

	/**
	 * Listens to a single child's state.
	 */
	class StateListener implements JobStateListener {

		private final StateHolder holder;
		
		public StateListener(StateHolder holder) {
			this.holder = holder;
		}
		
		public synchronized void jobStateChange(JobStateEvent event) {
			holder.state = event.getJobState();
			
			checkStates();
		};
	};
	
	/**
	 * Constructor.
	 * 
	 * @param structural
	 * @param operator
	 */
	public StructuralStateHelper(Structural structural, StateOperator operator) {
		this.operator = operator;
		
		// Add a listener that tracks child changes.
		structural.addStructuralListener(
				new StructuralListener() {
			public void childAdded(StructuralEvent event) {
				synchronized(states) {
					int index = event.getIndex();
					Object child = event.getChild();
	
					StateHolder stateHolder = new StateHolder();
	
					StateListener listener = new StateListener(stateHolder);
					listeners.add(index, listener);
					
					states.add(index, stateHolder);
	
					if (child instanceof Stateful) {
						((Stateful) child).addJobStateListener(listener);
					}
					else {
						checkStates();
					}
				}
			}
				
			public void childRemoved(StructuralEvent event) {
				synchronized(states) {					
					int index = event.getIndex();
					Object child = event.getChild();


					JobStateListener listener = listeners.remove(index);

					if (child instanceof Stateful) {
						((Stateful) child).removeJobStateListener(listener);
					}

					states.remove(index);

					checkStates();
				}
			}				
		});
	}
	
	public void addJobStateListener(JobStateListener listener) {
		stateHandler.addJobStateListener(listener);
	}
	
	public void removeJobStateListener(JobStateListener listener) {
		stateHandler.removeJobStateListener(listener);		
	}
			
	private void checkStates() {
		
		stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				JobState[] stateArgs = new JobState[states.size()];
				int i = 0;
				for (StateHolder holder : states) {
					stateArgs[i++] = holder.state; 
				}
				
				JobState state = operator.evaluate(stateArgs);
						
				if (state == stateHandler.getJobState()) {
					return;
				}
				
				stateHandler.setJobState(state);
				stateHandler.fireEvent();
			}
		});
	}

	@Override
	public JobStateEvent lastJobStateEvent() {
		return stateHandler.lastJobStateEvent();
	}
	
	public JobState[] getChildStates() {
		synchronized(states) {
			JobState[] array = new JobState[states.size()];
			int i = 0;
			for (StateHolder holder : states) {
				array[i++] = holder.state;
			}
			return array;
		}
	}
	
}
