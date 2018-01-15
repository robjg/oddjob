/*
 * Based heavily on the Log4j NDC published under the terms of the Apache Software License
 * version 1.1 
 */
package org.oddjob.framework;

import java.util.Stack;

import org.oddjob.util.Restore;

/**
 * Stack of class loaders.
 * 
 * @author rob
 */
public class ContextClassloaders {
	
	/** The stack of contexts. */
	private static InheritableThreadLocal<Stack<ClassLoader>> local = 
			new InheritableThreadLocal<Stack<ClassLoader>>() {
		@Override
		protected Stack<ClassLoader> initialValue() {
			return new Stack<ClassLoader>();
		}
		@Override
		@SuppressWarnings("unchecked")
		protected Stack<ClassLoader> childValue(Stack<ClassLoader> parentValue) {
				return (Stack<ClassLoader>)parentValue.clone();
		}
	};
	
	// No instances allowed.
	private ContextClassloaders() {
	}

	/**
	 * Replace context class loader.
	 * 
	 */
	public static void pop() {
		Stack<ClassLoader> stack = local.get();
		ClassLoader last = stack.pop();
		Thread.currentThread().setContextClassLoader(last);
	}


	/**
	 * Use current components class loader as context class Loader.
	 * 
	 * @param component
	 *            The new current component.
	 * 
	 */
	public static Restore push(Object component) {
		if (component == null) {
			throw new NullPointerException("Can't push null job.");
		}
		
		Stack<ClassLoader> stack = local.get();

		Thread currentThread = Thread.currentThread();
		
		stack.push(currentThread.getContextClassLoader());
		
		Thread.currentThread().setContextClassLoader(
				component.getClass().getClassLoader());
		
		return new Restore() {
			
			@Override
			public void close() {
				if (Thread.currentThread() != currentThread) {
					throw new IllegalStateException("Restore can only happen on same thread as push.");
				}

				pop();
			}
		};
	}

}
