/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.beanbus.mega;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.framework.adapt.WrapperInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

/**
 * Possibly provide a proxy to use as the component. 
 * 
 *
 * @author Rob Gordon.
 */
public class MegaBusComponentResolver 
implements ComponentProxyResolver {
	
	private final ComponentProxyResolver existing;
	
	public MegaBusComponentResolver(ComponentProxyResolver existing) {
		this.existing = existing;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object resolve(final Object component, ArooaSession session) {

		Object proxy;
		
		if (component instanceof BusPart) {
			proxy = component;
		}
		else if (component instanceof Consumer) {
	    	proxy = new ConsumerProxyGenerator(session).generate(
	    			(Consumer<?>) component,
	    			component.getClass().getClassLoader());
	    }
	    else {
	    	proxy = existing.resolve(component, session);
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
