package org.oddjob.state;

import org.oddjob.Stateful;
import org.oddjob.Structural;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Track, and aggregate the states of child jobs. Aggregation is
 * achieved using the given {@link StateOperator}.
 * 
 * @author rob
 *
 */
public class StructuralStateHelper implements Stateful {
	private static final Logger logger = LoggerFactory.getLogger(
			StructuralStateHelper.class);

	/** The structural we're helping. */
	private final Structural structural;
	
	/** Used to capture state into. */
	private final ParentStateHandler stateHandler = 
		new ParentStateHandler(this);
	
	/** The states of the children. *
	 * References are required because the position of the children
	 * can move. The state listener uses a reference instead
	 * of an index to insert state.
	 */
	private final List<AtomicReference<StateEvent>> childStateEvents =
		new ArrayList<>();
	
	/** The listeners listening to the children. */
	private final List<StateListener> listeners = 
		new ArrayList<>();

	/** The {@link StateOperator}. */
	private volatile StateOperator stateOperator;

	/**
	 * Listens to a single child's state.
	 */
	class ChildStateListener implements StateListener {

		private final AtomicReference<StateEvent> holder;
		
		public ChildStateListener(AtomicReference<StateEvent> holder) {
			this.holder = holder;
		}
		
		@Override
		public void jobStateChange(final StateEvent event) {
			
			stateHandler.runLocked(() -> {
				StateEvent previous = holder.getAndSet(event);

				// Don't check when listener initially added as this happens
				// in when child added.
				if (previous != null) {
					checkStates();
				}
			});
		}
		
		@Override
		public String toString() {
			return getClass().getName() + " for [" + structural + "]";
		}
	}

	/**
	 * Create a new instance that will track state changes in the children
	 * of the given {@link Structural}. States of the children will be
	 * combined using the given {@link StateOperator}.
	 * 
	 * @param structural The structural. Must not be null.
	 * @param operator The operator that combines child state. Must not be
	 * null.
	 */
	public StructuralStateHelper(Structural structural, StateOperator operator) {
		if (structural == null) {
			throw new NullPointerException("No Structural.");
		}
		if (operator == null) {
			throw new NullPointerException("No State Operator.");
		}
		
		this.structural = structural;
		
		setStateOperator(operator);
		
		// Add a listener that tracks child changes.
		structural.addStructuralListener(
				new StructuralListener() {
					
			@Override
			public void childAdded(final StructuralEvent event) {
				stateHandler.runLocked(() -> {
					int index = event.getIndex();
					Object child = event.getChild();

					AtomicReference<StateEvent> stateHolder =
							new AtomicReference<>();

					ChildStateListener listener =
							new ChildStateListener(stateHolder);

					if (child instanceof Stateful) {
						((Stateful) child).addStateListener(listener);
					}
					else {
						stateHolder.set(new ConstStateful(JobState.COMPLETE).lastStateEvent());
					}

					listeners.add(index, listener);
					childStateEvents.add(index, stateHolder);

					checkStates();
				});
			}
				
			@Override
			public void childRemoved(final StructuralEvent event) {
				stateHandler.runLocked(() -> {
					int index = event.getIndex();
					Object child = event.getChild();

					StateListener listener = listeners.remove(index);

					if (child instanceof Stateful) {
						((Stateful) child).removeStateListener(listener);
					}

					childStateEvents.remove(index);

					checkStates();
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
		
		stateHandler.runLocked(() -> {
			StateEvent[] stateArgs = childStateEvents.stream()
					.map(AtomicReference::get)
					.toArray(StateEvent[]::new);

			StateEvent stateEvent = stateOperator.evaluate(stateArgs);

			if (stateEvent == null) {
				if (stateHandler.getState() == ParentState.READY) {
					return;
				}
				stateHandler.setState(ParentState.READY);
			}
			else {
				// don't fire a new state if it is the same as the last.
				if (stateEvent.equals(stateHandler.lastStateEvent())) {
					return;
				}

				if (stateEvent.getState().isException()) {
					stateHandler.setStateException((ParentState) stateEvent.getState(),
							stateEvent.getException(), stateEvent.getTime());
				}
				else {
					stateHandler.setState((ParentState) stateEvent.getState(), stateEvent.getTime());
				}
			}
			stateHandler.fireEvent();
		});
	}

	@Override
	public StateEvent lastStateEvent() {
		return stateHandler.lastStateEvent();
	}
	
	public StateEvent[] getChildStates() {
		
		return stateHandler.supplyLocked(() -> childStateEvents.stream()
				.map(AtomicReference::get)
				.toArray(StateEvent[]::new));
	}

	public StateOperator getStateOperator() {
		return stateOperator;
	}

	/**
	 * Change the State Operator to be used to combine child state.
	 * 
	 * @param stateOperator The State Operator. Must not be null.
	 */
	public void setStateOperator(StateOperator stateOperator) {
		
		if (stateOperator == null) {
			throw new NullPointerException("State Operator must not be null."); 
		}

		if (stateOperator.equals(this.stateOperator)) {
			return;
		}

		if (this.stateOperator != null) {
			logger.info("Changing State Operator from " + this.stateOperator +
					" to " + stateOperator);
		}
		
		this.stateOperator = stateOperator;
		checkStates();
	}

}
