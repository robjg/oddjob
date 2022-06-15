/*
 * (c) Rob Gordon 2005
 */
package org.oddjob;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.framework.adapt.WrapperInvocationHandler;
import org.oddjob.framework.adapt.job.JobProxyGenerator;
import org.oddjob.framework.adapt.job.JobStrategies;
import org.oddjob.framework.adapt.service.ServiceProxyGenerator;
import org.oddjob.framework.adapt.service.ServiceStrategies;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Optional;

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

        if (component instanceof Stateful) {
            return component;
        }

        Optional<Object> proxy;

        proxy = new ServiceStrategies().adapt(component, session)
                        .map(serviceAdaptor -> new ServiceProxyGenerator().generate(serviceAdaptor,
                                component.getClass().getClassLoader()));

        if (proxy.isPresent()) {
            return proxy.get();
        }

        proxy = new JobStrategies().adapt(component, session)
                .map(jobAdaptor -> new JobProxyGenerator().generate(jobAdaptor,
                        component.getClass().getClassLoader()));

        return proxy.orElse(component);
    }

    @Override
    public Object restore(Object proxy, ArooaSession session) {

        Object component;

        if (!Proxy.isProxyClass(proxy.getClass())) {
            component = proxy;
        } else {
            InvocationHandler handler = Proxy.getInvocationHandler(proxy);
            component = ((WrapperInvocationHandler) handler).getWrappedComponent();
        }

        return component;
    }

}
