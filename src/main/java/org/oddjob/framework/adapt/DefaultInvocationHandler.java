package org.oddjob.framework.adapt;

import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.util.Restore;

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
		
		this.methods = new HashMap<>();
		
		 {
			 Class<?>[] interfaces = wrappedInterfaces;
             for (Class<?> anInterface : interfaces) {
                 addMethods(anInterface, wrapped);
             }
		 }
		 
		 {
			 Class<?>[] interfaces = wrappingInterfaces;
             for (Class<?> anInterface : interfaces) {
                 addMethods(anInterface, wrapper);
             }
		 }
	}
	
	/**
	 * Add a method and the object that is going to implement it.
	 * 
	 * @param from The interface the method is from
	 * @param destination The object or wrapper to direct the method to.
	 */
	private void addMethods(Class<?> from, Object destination) {
		Method[] ms = from.getMethods();
        for (Method m : ms) {
        	if (!m.getDeclaringClass().isInstance(destination)) {
        		throw new IllegalArgumentException("Destination " + destination + " is not an instance of " + m.getDeclaringClass());
			}
            methods.put(m, destination);
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
			throw new IllegalStateException("Unknown method " + method +
					" for [" + wrapped + "].");
		}

		try (Restore ignored = ComponentBoundary.push(
				wrapper.loggerName(), wrapped)) {

            return method.invoke(destination, args);
        }
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
