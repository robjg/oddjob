package org.oddjob.beanbus;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Abstract base class for {@link BeanBus} destinations.
 * 
 * @author rob
 *
 * @param <E>
 */
abstract public class AbstractDestination<E> implements Collection<E>  {
	
	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean contains(Object o) {
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		Set<E> empty = Collections.emptySet();
		return empty.iterator();
	}

	@Override
	public Object[] toArray() {
		Set<E> empty = Collections.emptySet();
		return empty.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		Set<T> empty = Collections.emptySet();
		return empty.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		for (E e : c) {
			add(e);
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {
	}

}
