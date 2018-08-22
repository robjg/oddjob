/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * A counter which counts many things and executes an action 
 * when the count of things reaches zero or a thing
 * is first created.
 * 
 * @author Rob Gordon.
 */
public class SimpleCounter {

	/** A Map of things to the count of the things. */
	private Map<Object, Integer> counter = 
		new HashMap<Object, Integer>();

	/**
	 * Add an object to be counted without an action.
	 * 
	 * @param key The object to count.
	 */
	public void add(Object key) {
		add(key, null);
	}
	
	/**
	 * Add a thing to be counted, and the action to execute if this
	 * is the first thing to be counted.
	 * 
	 * @param key The thing to count.
	 * @param newAction The action to take if this is the first thing.
	 */
	synchronized public void add(Object key, Runnable newAction) {
		Integer count = (Integer) counter.get(key);
		if (count == null) {
			count = new Integer(1);
			if (newAction != null) {
				newAction.run();
			}
		} else {
			count = new Integer(count.intValue() + 1);
		}
		counter.put(key, count);
	}
	
	/**
	 * Remove an object taking no action if the count of that object
	 * reaches 0.
	 * 
	 * @param key The object to remove from the count.
	 * 
	 * @throws IllegalStateException If the object was never counted.
	 */
	public void remove(Object key) 
	throws IllegalStateException {
		remove(key, null);
	}
	
	/**
	 * Remove an object from the count. If the count is zero after 
	 * removal execute the given action.
	 * 
	 * @param key The object that will decrement the count.
	 * @param emptyAction Action to take when the count is 0.
	 * 
	 * @throws IllegalStateException If the object was never counted.
	 */
	synchronized public void remove(Object key, Runnable emptyAction) 
	throws IllegalStateException {
		Integer count = (Integer) counter.get(key);
		if (count == null) {
			return;
		}
		int c = count.intValue();
		if (c == 1) {
			counter.remove(key);
			if (emptyAction != null) {
				emptyAction.run();
			}
		} else {
			count = new Integer(c - 1);				
		}
	}
	
}
