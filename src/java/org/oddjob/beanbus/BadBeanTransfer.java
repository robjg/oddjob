package org.oddjob.beanbus;

import java.io.Serializable;

public class BadBeanTransfer<T> implements Serializable {
	private static final long serialVersionUID = 2013020800L;
	
	private final T badBean;
	
	private final IllegalArgumentException exception;
	
	public BadBeanTransfer(T badBean, IllegalArgumentException e) {
		this.badBean = badBean;
		this.exception = e;
	}

	public T getBadBean() {
		return badBean;
	}
	
	public IllegalArgumentException getException() {
		return exception;
	}
}
