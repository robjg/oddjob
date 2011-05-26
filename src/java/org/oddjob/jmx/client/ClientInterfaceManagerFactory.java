/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Client side utility class for managing ClientInterfaceHandlers.
 *
 * @author Rob Gordon
 */
class ClientInterfaceManagerFactory {

	private final Set<Class<?>> interfaces = new HashSet<Class<?>>();

	private final Set<ClientInterfaceHandlerFactory<?>> clientHandlerFactories = 
		new HashSet<ClientInterfaceHandlerFactory<?>>();
	
	public ClientInterfaceManagerFactory(
			ClientInterfaceHandlerFactory<?>[] clientHandlerFactories) {

		for (int i = 0; clientHandlerFactories != null && i < clientHandlerFactories.length; ++i) {
			ClientInterfaceHandlerFactory<?> handlerFactory = clientHandlerFactories[i];
			addHandlerFactory(handlerFactory);
		}
	}
		
	public void addHandlerFactory(ClientInterfaceHandlerFactory<?> handlerFactory) {
		
		// add to factories.
		this.clientHandlerFactories.add(handlerFactory);
		
		if (handlerFactory.interfaceClass().isInterface()) {
			// add to interfaces supported.
			interfaces.add(handlerFactory.interfaceClass());
		}
	}
		
	public Class<?>[] interfaces() {
		return (Class[]) interfaces.toArray(new Class[0]);
	}
			
	public ClientInterfaceManager create(
			Object source, 
			ClientSideToolkit csToolkit) {

		/** Map of methods of the InterfaceHandlers. Not sure if the order
		 * interface might be important but we are using a LinkedHashMap just
		 * in case it is. */
		final Map<Method, Operation<?>> operations = 
			new LinkedHashMap<Method, Operation<?>>();

		// Loop over all definitions.
		for (Iterator<ClientInterfaceHandlerFactory<?>> it = clientHandlerFactories.iterator(); it.hasNext(); ) {
			ClientInterfaceHandlerFactory<?> clientHandlerFactory = it.next();

			createOperations(source, csToolkit, clientHandlerFactory, operations);
		}
		
		return new ClientInterfaceManager() {
			public Object invoke(Method method, Object[] args)
			throws Throwable {
				Operation<?> op = (Operation<?>) operations.get(method);
				
				if (op == null) {
					throw new IllegalArgumentException("No interface supports method [" + method + "]");
				}
				
				Object interfaceHandler = op.getHandler();
				
				try {
					return method.invoke(interfaceHandler, 
								args);
				} catch (InvocationTargetException e) {
					throw e.getTargetException();
				}
			}

		};
	}

	/**
	 * Purely for Template trickary.
	 * 
	 * @param <T>
	 * @param source
	 * @param csToolkit
	 * @param factory
	 * @param operations
	 */
	private <T> void createOperations(
			Object source, 
			ClientSideToolkit csToolkit,
			ClientInterfaceHandlerFactory<T> factory, 
			Map<Method, Operation<?>> operations) {

		Class<T> cl = factory.interfaceClass();

		// create the interface handler
		T interfaceHandler
			= factory.createClientHandler(cl.cast(source), csToolkit);

		// map operations to handler
		Method[] methods = cl.getMethods();
		
		for (int j = 0; j < methods.length; ++j) {
			Method m = methods[j];
			
			Operation<?> op = operations.get(m);
			
			if (op != null) {
				throw new IllegalArgumentException("Method [" + 
						m + "] already registered by factory for " +
						op.getFactory().interfaceClass().getName());
			}
			
			operations.put(
					m, 
					new Operation<T>(interfaceHandler,
							factory));
		}
	}
	
	/**
	 * Store a handler and a factory for an operation.
	 * <p>
	 * The factory is only stored for to create the duplicate message. Can't remember why
	 * this the handler wasn't good enough for the message.
	 *
	 * 
	 * @author rob
	 *
	 */
	class Operation<T> {
		
		private final T handler;
		private final ClientInterfaceHandlerFactory<T> factory;
		
		Operation(T handler,
				ClientInterfaceHandlerFactory<T> factory) {
			this.handler = handler;
			this.factory = factory;
		}
		
		T getHandler() {
			return handler;
		}
		
		ClientInterfaceHandlerFactory<T> getFactory() {
			return factory;
		}
	}
}