package org.oddjob.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Default invocation handler for Proxies for wrapped components.
 * 
 * @author rob
 *
 */
public class DefaultInvocationHandler 
implements WrapperInvocationHandler, Serializable {
	private static final long serialVersionUID = 2012012200L;
	
	/**
	 * Map of methods to the object that it will be invoked on.
	 */
	private transient Map<Method, Object> methods;
	
	private ComponentWrapper wrapper;
	
	private Class<?>[] wrappingInterfaces;
	
	private Object wrapped;
	
	private Class<?>[] wrappedInterfaces;
	
	/**
	 * Initialise the invocation handler.
	 * 
	 * @param wrapper The wrapper. Handles invocations for wrapping 
	 * interfaces.
	 * @param wrappingInterfaces The wrapping interfaces.
	 * @param wrapped The component.
	 * @param wrappedInterfaces The component interfaces.
	 */
	public void initialise(ComponentWrapper wrapper, 
			Class<?>[] wrappingInterfaces,
			Object wrapped, 
			Class<?>[] wrappedInterfaces) {
		
		this.wrapper = wrapper;
		this.wrappingInterfaces = wrappingInterfaces;
		this.wrapped = wrapped;
		this.wrappedInterfaces = wrappedInterfaces;
		
		initialiseMethods();
	}
	
	private void initialiseMethods() {
		
		this.methods = new HashMap<Method, Object>();
		
		 {
			 Class<?>[] interfaces = wrappedInterfaces;
			 for (int i = 0; i < interfaces.length; ++i) {
				 addMethods(interfaces[i], wrapped);
			 }
		 }
		 
		 {
			 Class<?>[] interfaces = wrappingInterfaces;
			 for (int i = 0; i < interfaces.length; ++i) {
				 addMethods(interfaces[i], wrapper);
			 }
		 }
	}
	
	/**
	 * Add a method and the object that is going to implement it.
	 * 
	 * @param from
	 * @param destination
	 */
	private void addMethods(Class<?> from, Object destination) {
		Method[] ms = from.getDeclaredMethods();
		for (int i = 0; i < ms.length; ++i) {
			methods.put(ms[i], destination);
		}
	}	
	
	@Override
	public Object getWrappedComponent() {
		return wrapped;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		Object destination = methods.get(method);
		if (destination == null) {
			throw new IllegalStateException("Unknown method " + method);
		}
		
		return method.invoke(destination, args);
	}
	
	/**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) throws IOException,
			ClassNotFoundException {
		s.defaultReadObject();
		initialiseMethods();
	}

}
