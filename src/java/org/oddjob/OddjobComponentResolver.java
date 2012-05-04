/*
 * (c) Rob Gordon 2005
 */
package org.oddjob;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.framework.CallableProxyGenerator;
import org.oddjob.framework.ServiceStrategies;
import org.oddjob.framework.RunnableProxyGenerator;
import org.oddjob.framework.ServiceAdaptor;
import org.oddjob.framework.ServiceProxyGenerator;
import org.oddjob.framework.WrapperInvocationHandler;

/**
 * Possibly provide a proxy to use as the component. The proxy will provide
 * State, Icon information etc for simple components. 
 * <p>
 * The logic here is quite simple:
 * <ul>
 * <li>If the component is {@link Stateful} it is used as is.</li>
 * <li>If the component is {@link Runnable} it is proxied.</li>
 * <li>If the component provides a service like interface it is proxied.</li>
 * <li>Otherwise it is used as is.</li>
 * </ul>
 *
 * @author Rob Gordon.
 */
public class OddjobComponentResolver 
implements ComponentProxyResolver {
	
	@Override
	public Object resolve(final Object component, ArooaSession session) {

		Object proxy;
		
	    if (component instanceof Stateful) {
	    	proxy = component;
	    }
	    else if (component instanceof Callable){
	    	proxy = new CallableProxyGenerator().generate(
	    			(Callable<?>) component, 
	    			component.getClass().getClassLoader());
	    }
	    else if (component instanceof Runnable){
	    	proxy = new RunnableProxyGenerator().generate(
	    			(Runnable) component,
	    			component.getClass().getClassLoader());
	    }
	    else {
	    	ServiceAdaptor service = 
	    			new ServiceStrategies().serviceFor(component);
	    	if (service != null) {
	    		proxy = new ServiceProxyGenerator().generate(service, 
	    				component.getClass().getClassLoader());
	    	}
	    	else {
	    		proxy = component;
	    	}
	    }
	    
	    return proxy;
	}
	
	@Override
	public Object restore(Object proxy, ArooaSession session) {
		
		Object component;
		
		if (!Proxy.isProxyClass(proxy.getClass())) {
			component = proxy;
		}
		else {
			InvocationHandler handler = Proxy.getInvocationHandler(proxy);
			component = ((WrapperInvocationHandler) handler).getWrappedComponent();
		}

		return component;
	}
	
}
