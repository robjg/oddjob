package org.oddjob.beanbus;

import java.util.Collection;

/**
 * For Standard Filter Components to extend. 
 * 
 * @author rob
 *
 * @param <F> From Type
 * @param <T> To Type
 */
abstract public class AbstractFilter<F, T> extends AbstractDestination<F> 
implements BusFilter<F, T> {

	private Collection<? super T> to;
	
	private String name;
	
	@Override
	public final boolean add(F bean) {

		T filtered = filter(bean);
		
		if (filtered == null) {
			return false;
		}
		else if (to == null) {
			return false;
		}
		else {
			to.add(filtered);
			return true;
		}
	}
	
	abstract protected T filter(F from);
	
	public Collection<? super T> getTo() {
		return to;
	}

	@Override
	public void setTo(Collection<? super T> to) {
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
