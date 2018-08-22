package org.oddjob.framework.adapt;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates a Proxy for a wrapped component. The proxy provided
 * will implement all interfaces from the original component as well
 * as those provided by the {@link WrapperFactory}.
 * <p>
 * If the wrapped component is an instance of an {@link ComponentAdapter} the 
 * underlying component interface will be used instead.
 * 
 * @author rob
 *
 * @param <T> The type of the component being wrapped.
 */
public class ProxyGenerator<T> {

	/**
	 * Generate the proxy.
	 * 
	 * @param wrapped The component being wrapped.
	 * @param wrapperFactory
	 * @param classLoader
	 * 
	 * @return A proxy implementing all the interface of factory and
	 * component.
	 */
	public Object generate(T wrapped, WrapperFactory<T> wrapperFactory, 
			ClassLoader classLoader) {

		Object component;
		if (wrapped instanceof ComponentAdapter) {
			component = ((ComponentAdapter) wrapped).getComponent();
		}
		else {
			component = wrapped;
		}
		
		Class<?>[] wrappedInterfaces = 
				interfacesFor(component);
		Class<?>[] wrappingInterfaces = 
				wrapperFactory.wrappingInterfacesFor(wrapped);
		
    	Set<Class<?>> proxyInterfaces = new HashSet<Class<?>>();
    	proxyInterfaces.addAll(Arrays.asList(wrappedInterfaces));
    	proxyInterfaces.addAll(Arrays.asList(wrappingInterfaces));
    	proxyInterfaces.remove(Object.class);
    	
    	Class<?>[] interfaceArray = 
        		(Class[]) proxyInterfaces.toArray(new Class[proxyInterfaces.size()]);
    	
    	DefaultInvocationHandler handler = new DefaultInvocationHandler();
    	
    	Object proxy = 
        		Proxy.newProxyInstance(classLoader,
        			interfaceArray,
        			handler);

    	ComponentWrapper wrapper = wrapperFactory.wrapperFor(wrapped, proxy);
    	
    	handler.initialise(wrapper, 
    			wrappingInterfaces, 
    			component, 
    			wrappedInterfaces);
    	
        return proxy;
    	
	}
	
	/**
	 * Find all the interfaces an object implements.
	 * 
	 * @param object The object.
	 * @return All the interfaces it implements
	 */
    public static Class<?>[] interfacesFor(Object object) {
    	List<Class<?>> results = new ArrayList<Class<?>>();
    	for (Class<?> cl = object.getClass(); cl != null; cl = cl.getSuperclass()) {
    		results.addAll(Arrays.asList((Class<?>[]) cl.getInterfaces()));
    	}
    	return (Class[]) results.toArray(new Class[0]);
    }     
}
