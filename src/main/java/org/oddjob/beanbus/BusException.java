package org.oddjob.beanbus;

abstract public class BusException extends Exception {
	private static final long serialVersionUID = 2010021700L;

	public BusException() {
		super();
	}

	public BusException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusException(String message) {
		super(message);
	}

	public BusException(Throwable cause) {
		super(cause);
	}

	
}
