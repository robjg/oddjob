package org.oddjob.beanbus;

public class CrashBusException extends BusException {
	private static final long serialVersionUID = 2010021900L;

	public CrashBusException() {
		super();
	}

	public CrashBusException(String message, Throwable cause) {
		super(message, cause);
	}

	public CrashBusException(String message) {
		super(message);
	}

	public CrashBusException(Throwable cause) {
		super(cause);
	}

	
}
