package org.oddjob.beanbus;

import java.util.function.Consumer;

/**
 * For Standard Filter Components to extend. 
 * 
 * @author rob
 *
 * @param <F> From Type
 * @param <T> To Type
 */
abstract public class AbstractFilter<F, T> implements Consumer<F>, BusFilter<F, T> {

	private Consumer<? super T> to;
	
	private String name;

	@Override
	public void accept(F bean) {

		T filtered = filter(bean);
		
		if (filtered != null && to != null) {
			to.accept(filtered);
		}
	}
	
	abstract protected T filter(F from);
	
	public Consumer<? super T> getTo() {
		return to;
	}

	@Override
	public void setTo(Consumer<? super T> to) {
		this.to = to;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {

		if (name == null) {
			return getClass().getSimpleName();
		}
		else {
			return name;
		}
	}
}
