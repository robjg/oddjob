package org.oddjob.state;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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

	/** The structural we're helping. */
	private final Structural structural;
	
	/** Used to capture state into. */
	private final ParentStateHandler stateHandler = 
		new ParentStateHandler(this);
	
	/** The states of the children. */
	private final List<StateHolder> states = 
		new ArrayList<StateHolder>();
	
	/** The listeners listening to the children. */
	private final List<StateListener> listeners = 
		new ArrayList<StateListener>();

	/** The {@link StateOperator}. */
	private final StateOperator operator;

	/** Holder is required because the position of the children
	 * can move. The state listener uses a holder instead
	 * of an index to insert state.
	 */
	class StateHolder {
		private State state = ParentState.COMPLETE;
	}

	/**
	 * Listens to a single child's state.
	 */
	class ChildStateListener implements StateListener {

		private final StateHolder holder;
		
		public ChildStateListener(StateHolder holder) {
			this.holder = holder;
		}
		
		@Override
		public synchronized void jobStateChange(StateEvent event) {
			holder.state = event.getState();
			
			checkStates();
		};
		
		@Override
		public String toString() {
			return getClass().getName() + " for [" + structural + "]";
		}
	};
	
	/**
	 * Constructor.
	 * 
	 * @param structural
	 * @param operator
	 */
	public StructuralStateHelper(Structural structural, StateOperator operator) {
		this.structural = structural;
		this.operator = operator;
		
		// Add a listener that tracks child changes.
		structural.addStructuralListener(
				new StructuralListener() {
					
			@Override
			public void childAdded(final StructuralEvent event) {
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						int index = event.getIndex();
						Object child = event.getChild();
		
						StateHolder stateHolder = new StateHolder();
		
						ChildStateListener listener = new ChildStateListener(stateHolder);
						listeners.add(index, listener);
						
						states.add(index, stateHolder);
		
						if (child instanceof Stateful) {
							((Stateful) child).addStateListener(listener);
						}
						else {
							checkStates();
						}
					}
				});
			}
				
			@Override
			public void childRemoved(final StructuralEvent event) {
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						int index = event.getIndex();
						Object child = event.getChild();


						StateListener listener = listeners.remove(index);

						if (child instanceof Stateful) {
							((Stateful) child).removeStateListener(listener);
						}

						states.remove(index);

						checkStates();
					}
				});
			}				
		});
	}
	
	public void addStateListener(StateListener listener) {
		stateHandler.addStateListener(listener);
	}
	
	public void removeStateListener(StateListener listener) {
		stateHandler.removeStateListener(listener);		
	}
			
	private void checkStates() {
		
		stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				State[] stateArgs = new State[states.size()];
				int i = 0;
				for (StateHolder holder : states) {
					stateArgs[i++] = holder.state; 
				}
				
				ParentState state = operator.evaluate(stateArgs);
						
				if (state == stateHandler.getState()) {
					return;
				}
				
				stateHandler.setState(state);
				stateHandler.fireEvent();
			}
		});
	}

	@Override
	public StateEvent lastStateEvent() {
		return stateHandler.lastStateEvent();
	}
	
	public State[] getChildStates() {
		
		return stateHandler.callLocked(new Callable<State[]>() {
			@Override
			public State[] call() throws Exception {
				State[] array = new State[states.size()];
				int i = 0;
				for (StateHolder holder : states) {
					array[i++] = holder.state;
				}
				return array;
			}
		});
	}
}
