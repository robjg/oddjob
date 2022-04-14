package org.oddjob.beanbus.destinations;

import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusFilter;
import org.oddjob.framework.adapt.HardReset;
import org.oddjob.framework.adapt.SoftReset;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * An adaptor from an {@code java.util.function.Predicate} to a {@link BusFilter}.
 * 
 * @author rob
 *
 * @param <T>
 */
public class BeanFilter<T> extends AbstractFilter<T, T>
implements BusFilter<T, T> {

	private Predicate<? super T> predicate;

	private final AtomicInteger passed = new AtomicInteger();

	private final AtomicInteger blocked = new AtomicInteger();

	@Override
	protected T filter(T from) {
		if (predicate.test(from)) {
			passed.incrementAndGet();
			return from;
		}
		else {
			blocked.incrementAndGet();
			return null;
		}
	}

	@SoftReset
	@HardReset
	public void reset() {
		passed.set(0);
		blocked.set(0);
	}

	public int getPassed() {
		return passed.get();
	}

	public int getBlocked() {
		return blocked.get();
	}
		
	public Predicate<? super T> getPredicate() {
		return predicate;
	}

	public void setPredicate(Predicate<? super T> filter) {
		this.predicate = filter;
	}	
}
