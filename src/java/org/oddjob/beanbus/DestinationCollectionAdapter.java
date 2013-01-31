package org.oddjob.beanbus;

import java.util.Collection;

/**
 * A {@link Destination} that add beans to a collection.
 * 
 * @author rob
 *
 * @param <T> The type of the beans to be collected.
 */
public class DestinationCollectionAdapter<T> implements Destination<T> {

	private Collection<? super T> collection;
	
	public void accept(T bean) {
		collection.add(bean);
	};
	
	public void setCollection(Collection<? super T> collection) {
		this.collection = collection;
	}
	
	public Collection<? super T> getCollection() {
		return collection;
	}
}
