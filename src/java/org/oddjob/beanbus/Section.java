package org.oddjob.beanbus;

public interface Section<F, T> extends Destination<F> {

	public void setTo(Destination<? super T> to);
}
