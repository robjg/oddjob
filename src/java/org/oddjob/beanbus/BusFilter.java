package org.oddjob.beanbus;

import java.util.Collection;

public interface BusFilter<F, T> extends Collection<F> {

	public void setTo(Collection<? super T> to);
}
