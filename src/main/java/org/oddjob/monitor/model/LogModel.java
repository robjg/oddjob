package org.oddjob.monitor.model;

import java.util.Observable;

import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogListener;

/**
 */

public class LogModel extends Observable implements LogListener {
	
	public void setUnAvailable() {
		LogAction e = new UnavailableEvent();
		setChanged();
		notifyObservers(e);
	}

	public void setClear() {
		LogAction e = new ClearEvent();
		setChanged();
		notifyObservers(e);
	}
	
	public void logEvent(LogEvent event) {
		LogAction e = new MessageEvent (
				event.getMessage(), event.getLevel());		
		setChanged();
		notifyObservers(e);
	}

}
