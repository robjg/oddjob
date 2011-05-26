/*
 * (c) Rob Gordon 2005
 */
package org.oddjob;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.framework.BaseWrapper;
import org.oddjob.framework.RunnableWrapper;
import org.oddjob.framework.Service;
import org.oddjob.framework.ServiceWrapper;

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
class OddjobComponentResolver 
implements ComponentProxyResolver {
	
	public Object resolve(Object component, ArooaContext parentContext) {

		Object proxy;
		
	    if (component instanceof Stateful) {
	    	proxy = component;
	    }
	    else if (component instanceof Runnable){
	    	proxy = RunnableWrapper.wrapperFor(
	    			(Runnable) component, 
	    			component.getClass().getClassLoader());
	    }
	    else {
	    	Service service = Service.serviceFor(component);
	    	if (service != null) {
	    		proxy = ServiceWrapper.wrapperFor(service, 
	    				component.getClass().getClassLoader());
	    	}
	    	else {
	    		proxy = component;
	    	}
	    }
	    
	    return proxy;
	}
	
	public Object restore(Object proxy, ArooaContext parentContext) {
		
		Object component;
		
		if (!Proxy.isProxyClass(proxy.getClass())) {
			component = proxy;
		}
		else {
			InvocationHandler handler = Proxy.getInvocationHandler(proxy);
			component = ((BaseWrapper) handler).getWrapped();
		}

		return component;
	}
	
}
