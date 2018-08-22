package org.oddjob.beanbus;

public class BusCrashException extends BusException {
	private static final long serialVersionUID = 2010021900L;

	public BusCrashException() {
		super();
	}

	public BusCrashException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusCrashException(String message) {
		super(message);
	}

	public BusCrashException(Throwable cause) {
		super(cause);
	}

	
}
