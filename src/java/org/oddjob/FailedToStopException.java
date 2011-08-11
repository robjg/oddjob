package org.oddjob;

public class FailedToStopException extends Exception {
	private static final long serialVersionUID = 2010071900L;

	private final Object failedToStop;
	
	public FailedToStopException(Stateful failedToStop) {
		super("[" + failedToStop + "] failed to stop, state is " +
				failedToStop.lastStateEvent().getState() + ".");
		this.failedToStop = failedToStop;
	}

	public FailedToStopException(Object failedToStop, String message) {
		super(message);
		this.failedToStop = failedToStop;
	}
	
	public FailedToStopException(Object failedToStop, Throwable cause) {
		super(cause);
		this.failedToStop = failedToStop;
	}
	
	
	public FailedToStopException(Object failedToStop, String message, 
			Throwable cause) {
		super(message, cause);
		this.failedToStop = failedToStop;
	}
	
	public Object getFailedToStop() {
		return failedToStop;
	}
	
}
