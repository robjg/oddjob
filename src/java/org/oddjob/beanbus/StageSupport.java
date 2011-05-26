package org.oddjob.beanbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class StageSupport implements StageNotifier {

	private List<StageListener> batchListeners = 
		new ArrayList<StageListener>();
	
	private final StageNotifier source;
	
	public StageSupport(StageNotifier source) {
		this.source = source;
	}
	
	private Stack<StageEvent> events = new Stack<StageEvent>();
	
	public void fireBatchStarting(String description) {
		fireBatchStarting(description, null);
	}
	
	public void fireBatchStarting(String description, Object data) {
		
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
	
	public void fireBatchComplete() {
		
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
