/*
 * (c) Rob Gordon 2005
 */
package org.oddjob;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.framework.adapt.WrapperInvocationHandler;
import org.oddjob.framework.adapt.job.CallableProxyGenerator;
import org.oddjob.framework.adapt.job.RunnableProxyGenerator;
import org.oddjob.framework.adapt.service.ServiceAdaptor;
import org.oddjob.framework.adapt.service.ServiceProxyGenerator;
import org.oddjob.framework.adapt.service.ServiceStrategies;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

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

        ServiceAdaptor service =
                new ServiceStrategies().serviceFor(component, session);
        if (service != null) {
            return new ServiceProxyGenerator().generate(service,
                    component.getClass().getClassLoader());
        }

        if (component instanceof Callable) {
            return new CallableProxyGenerator().generate(
                    (Callable<?>) component,
                    component.getClass().getClassLoader());
        }

        if (component instanceof Runnable) {
            return new RunnableProxyGenerator().generate(
                    (Runnable) component,
                    component.getClass().getClassLoader());
        }

        return component;
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
