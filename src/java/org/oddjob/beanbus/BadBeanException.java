package org.oddjob.beanbus;

public class BadBeanException extends BusException {
	private static final long serialVersionUID = 2010021700L;

	private final Object badBean;
	
	public BadBeanException(Object badBean) {
		super();
		this.badBean = badBean;
	}

	public BadBeanException(Object badBean, String message, Throwable cause) {
		super(message, cause);
		this.badBean = badBean;
	}

	public BadBeanException(Object badBean, String message) {
		super(message);
		this.badBean = badBean;
	}

	public BadBeanException(Object badBean, Throwable cause) {
		super(cause);
		this.badBean = badBean;
	}

	public Object getBadBean() {
		return badBean;
	}
}
