package org.oddjob.jmx.client;

/**
 * Used as a stand in node when the client proxy can't be created.
 * 
 * @see ClientSessionImpl#create(long)
 * 
 * @author rob
 *
 */
public class NodeCreationFailed {

	private final Exception reason;
	
	public NodeCreationFailed(Exception reason) {
		this.reason = reason;
	}
	
	public Exception getReason() {
		return reason;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
