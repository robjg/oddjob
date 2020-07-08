/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Client side utility class for creating an {@link ClientInterfaceManagerFactory}.
 *
 * @author Rob Gordon
 */
public class ClientInterfaceManagerFactoryBuilder {

	private final Map<Class<?>, ClientInterfaceHandlerFactory<?>> clientHandlerFactories =
			new HashMap<>();

	public ClientInterfaceManagerFactoryBuilder addFactories(
			ClientInterfaceHandlerFactory<?>... clientHandlerFactories) {

		for (int i = 0; clientHandlerFactories != null && i < clientHandlerFactories.length; ++i) {
			ClientInterfaceHandlerFactory<?> handlerFactory = clientHandlerFactories[i];
			addFactory(handlerFactory);
		}

		return this;
	}
		
	public ClientInterfaceManagerFactoryBuilder addFactory(
			ClientInterfaceHandlerFactory<?> handlerFactory) {

		if (handlerFactory == null) {
			return this;
		}

		Class<?> interfaceClass = handlerFactory.interfaceClass();

		if (this.clientHandlerFactories.containsKey(interfaceClass)) {
			throw new IllegalArgumentException("A Client Interface Handler Factory is already registered for ["
					+ interfaceClass + "], handlerFactory [" + handlerFactory + "]");
		}

		// add to factories.
		this.clientHandlerFactories.put(interfaceClass, handlerFactory);

		return this;
	}

	public ClientInterfaceManagerFactoryBuilder addFromProvider(
			HandlerFactoryProvider handlerFactory) {
		if (handlerFactory == null) {
			return this;
		}
		ClientInterfaceHandlerFactory<?>[] factories = handlerFactory.getHandlerFactories();
		for (ClientInterfaceHandlerFactory<?> factory : factories) {
			addFactory(factory);
		}
		return this;
	}

	public ClientInterfaceManagerFactory build() {
		return new Impl(this.clientHandlerFactories);
	}

	static class Impl implements ClientInterfaceManagerFactory {

		private final Map<Class<?>, ClientInterfaceHandlerFactory<?>> clientHandlerFactories;

		Impl(Map<Class<?>, ClientInterfaceHandlerFactory<?>> clientHandlerFactories) {
			this.clientHandlerFactories = new HashMap<>(clientHandlerFactories);
		}


		public Class<?>[] filter(Class<?>[] remoteSupports) {
			return Arrays.stream(remoteSupports)
			.filter(clientHandlerFactories::containsKey)
					.filter(Class::isInterface)
					.toArray(Class[]::new);
		}

		@Override
		public ClientInterfaceManager create(
				Object source,
				ClientSideToolkit csToolkit) {

			/* Map of methods of the InterfaceHandlers. Not sure if the order
			 * interface might be important but we are using a LinkedHashMap just
			 * in case it is. */
			final Map<Method, Operation<?>> operations =
					new LinkedHashMap<>();

			final List<Destroyable> destroyables =
					new ArrayList<>();

			// Loop over all definitions.
			for (ClientInterfaceHandlerFactory<?> clientHandlerFactory :
					clientHandlerFactories.values()) {

				if (clientHandlerFactory.interfaceClass().isInstance(source)) {
					Object handler = createOperations(source, csToolkit,
							clientHandlerFactory, operations);

					if (handler instanceof Destroyable) {
						destroyables.add((Destroyable) handler);
					}
				}
			}

			return new ClientInterfaceManager() {
				public Object invoke(Method method, Object[] args)
						throws Throwable {
					Operation<?> op = operations.get(method);

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

				@Override
				public void destroy() {
					for (Destroyable destroyable : destroyables) {
						destroyable.destroy();
					}
				}

			};
		}
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
	private static <T> T createOperations(
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

		for (Method m : methods) {
			Operation<?> op = operations.get(m);

			if (op != null) {
				throw new IllegalArgumentException("Failed adding methods for Interface Hander [" +
						interfaceHandler + "], method [" +
						m + "] already registered by factory for " +
						op.getFactory().interfaceClass().getName());
			}

			operations.put(
					m,
					new Operation<>(interfaceHandler,
							factory));
		}
		
		return interfaceHandler;
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
	static class Operation<T> {
		
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