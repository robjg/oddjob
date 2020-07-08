package org.oddjob.jmx.client;

import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * A {@link ClientInterfaceHandlerFactory} that creates a client handler
 * that passes all method invocations straight to the MBean.
 * 
 * @author rob
 *
 * @param <T>
 */
public class DirectInvocationClientFactory<T> 
implements ClientInterfaceHandlerFactory<T> {
	
	private final Class<T> type;
	
	public DirectInvocationClientFactory(Class<T> type) {
		Objects.requireNonNull(type);
		this.type = type;
	}
	
	public HandlerVersion getVersion() {
		return new HandlerVersion(1, 0);
	}
	
	public T createClientHandler(T ignored, final ClientSideToolkit toolkit) {
		Object delegate = Proxy.newProxyInstance(
				type.getClassLoader(), 
				new Class<?>[] { type },
				(proxy, method, args) -> toolkit.invoke(
						MethodOperation.from(method), args));
		
		return type.cast(delegate);
	}
	
	public Class<T> interfaceClass() {
		return type;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ", type=" + type.getName();
	}
}
