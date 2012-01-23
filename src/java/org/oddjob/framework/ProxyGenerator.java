package org.oddjob.framework;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates a Proxy for a wrapped component.
 * 
 * @author rob
 *
 * @param <T> The type of the component being wrapped.
 */
public class ProxyGenerator<T> {

	public Object generate(T wrapped, WrapperFactory<T> wrapperFactory, 
			ClassLoader classLoader) {

		Class<?>[] wrappedInterfaces = 
				interfacesFor(wrapped);
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
    			wrapped, 
    			wrappedInterfaces);
    	
        return proxy;
    	
	}
	
    public static Class<?>[] interfacesFor(Object object) {
    	List<Class<?>> results = new ArrayList<Class<?>>();
    	for (Class<?> cl = object.getClass(); cl != null; cl = cl.getSuperclass()) {
    		results.addAll(Arrays.asList((Class<?>[]) cl.getInterfaces()));
    	}
    	return (Class[]) results.toArray(new Class[0]);
    }     
}
