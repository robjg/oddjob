package org.oddjob.beanbus;

import java.io.Serializable;
import java.util.Objects;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BadBeanTransfer<?> that = (BadBeanTransfer<?>) o;
		return Objects.equals(badBean, that.badBean) && exception.equals(that.exception);
	}

	@Override
	public int hashCode() {
		return Objects.hash(badBean, exception);
	}

	@Override
	public String toString() {
		return "BadBeanTransfer{" +
				"badBean=" + badBean +
				", exception=" + exception +
				'}';
	}
}
