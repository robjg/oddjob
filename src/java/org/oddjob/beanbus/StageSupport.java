package org.oddjob.beanbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Support for splitting a bus into stages. A stage will typically be
 * some kind of a batching.
 * <p>
 * This class supports nested stages. A stage need not complete before
 * another begins. Internally the stages are stored on a stack. Stages can
 * not overlap they can only be nested.
 * 
 * @author rob
 *
 */
public class StageSupport implements StageNotifier {

	/** The listeners for stage notification events. */
	private List<StageListener> batchListeners = 
		new ArrayList<StageListener>();
	
	/** The source of the events. */
	private final StageNotifier source;
	
	/** Stack events. */
	private final Stack<StageEvent> events = new Stack<StageEvent>();
	
	/**
	 * Constructor.
	 * 
	 * @param source
	 */
	public StageSupport(StageNotifier source) {
		this.source = source;
	}
	
	/**
	 * First a stage starting. Null data will be passed to listeners.
	 * 
	 * @param description Description of the stage. Useful for logging.
	 */
	public void fireStageStarting(String description) {
		fireStageStarting(description, null);
	}
	
	/**
	 * Fire a stage starting event.
	 * 
	 * @param description Description of the stage. Useful for logging.
	 * @param data Data to be passed to listeners.
	 */
	public void fireStageStarting(String description, Object data) {
		
		List<StageListener> copy = new ArrayList<StageListener>(batchListeners);

		StageEvent event = new StageEvent(
				source, 
				description, 
				data);
	
		events.push(event);
		
		for (StageListener listener : copy) {
			listener.stageStarting(event);
		}		
	}
	
	/**
	 * Fire a stage complete event.
	 */
	public void fireStageComplete() {
		
		List<StageListener> copy = new ArrayList<StageListener>(batchListeners);
		
		StageEvent event = events.pop();
		
		for (StageListener listener : copy) {
			listener.stageComplete(event);
		}		
	}

	@Override
	public void addStageListener(StageListener listener) {
		batchListeners.add(listener);
	}
	
	@Override
	public void removeStageListener(StageListener listener) {
		batchListeners.remove(listener);
	}

}
