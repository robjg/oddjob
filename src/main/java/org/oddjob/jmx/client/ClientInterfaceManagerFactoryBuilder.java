/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

import org.oddjob.arooa.ClassResolver;
import org.oddjob.remote.Implementation;
import org.oddjob.remote.Initialisation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Client side utility class for creating an {@link ClientInterfaceManagerFactory}.
 *
 * @author Rob Gordon
 */
public class ClientInterfaceManagerFactoryBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ClientInterfaceManagerFactoryBuilder.class);

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

        @Override
        public Prepared prepare(Implementation<?>[] remoteSupports, ClassResolver classResolver) {

            final Map<Class<?>, Implementation<?>> implementationMap =
                    new LinkedHashMap<>();

            for (Implementation<?> implementation : remoteSupports) {

                Class<?> implType = classResolver.findClass(implementation.getType());

                if (implType == null) {

                    logger.debug("No Class for remote supported {} so ignoring.", implementation.getType());
                    continue;
                }

                if (clientHandlerFactories.containsKey(implType)) {
                    implementationMap.put(implType, implementation);
                } else {
                    logger.debug("No Client Handler Factory for remote supported {} so ignoring.", implType.getName());
                }
            }

            final Class<?>[] supportedInterfaces = implementationMap.keySet()
                    .stream()
                    .filter(Class::isInterface)
                    .toArray(Class[]::new);

            return new Prepared() {

                @Override
                public Class<?>[] supportedInterfaces() {
                    return supportedInterfaces;
                }

                @Override
                public ClientInterfaceManager create(Object source, ClientSideToolkit csToolkit) {

                    /* Map of methods of the InterfaceHandlers. Not sure if the order
                     * interface might be important but we are using a LinkedHashMap just
                     * in case it is. */
                    final Map<Method, Object> methodMapping =
                            new LinkedHashMap<>();

                    for (Map.Entry<Class<?>, Implementation<?>> entry : implementationMap.entrySet()) {

                        Class<?> type = entry.getKey();

                        ClientInterfaceHandlerFactory<?> factory = clientHandlerFactories.get(type);

                        Implementation<?> implementation = entry.getValue();

                        Object handler = createHandler(source, csToolkit, factory, implementation);

                        // map operations to handler
                        Method[] methods = type.getMethods();

                        for (Method m : methods) {
                            Object op = methodMapping.get(m);

                            if (op != null) {
                                logger.debug("Ignoring method [" +
                                        m + "] already registered for " +
                                        handler.getClass());
                                continue;
                            }

                            methodMapping.put(m, handler);
                        }
                    }

                    return new ManagerImpl(methodMapping);
                }
            };
        }

        private static <T> T createHandler(
                Object source,
                ClientSideToolkit csToolkit,
                ClientInterfaceHandlerFactory<T> factory,
                Implementation<?> implementation) {

            Class<T> cl = factory.interfaceClass();

            T sourceCast = cl.cast(source);

            if (implementation.getInitialisation() == null) {

                logger.trace("Creating client handler for [{}] from factory {} without initialisation.",
                        csToolkit, factory);
                return factory.createClientHandler(sourceCast, csToolkit);

            } else {

                Initialisation<?> initialisation  = implementation.getInitialisation();
                logger.trace("Creating client handler for [{}] from factory {} with initialisation [{}].",
                        csToolkit, factory, initialisation);
                return factory.createClientHandler(sourceCast,
                        csToolkit, initialisation);
            }
        }

        static class ManagerImpl implements ClientInterfaceManager {

            private final Map<Method, Object> methodMapping;

            ManagerImpl(Map<Method, Object> methodMapping) {
                this.methodMapping = methodMapping;
            }

            @Override
            public Object invoke(Method method, Object[] args)
                    throws Throwable {

                Object interfaceHandler = methodMapping.get(method);

                if (interfaceHandler == null) {
                    throw new IllegalArgumentException("No interface supports method [" + method + "]");
                }

                try {
                    return method.invoke(interfaceHandler,
                            args);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }

            @Override
            public void destroy() {

                Set<Destroyable> destroyables = methodMapping.values().stream()
                        .filter(o -> o instanceof Destroyable)
                        .map(o -> (Destroyable) o)
                        .collect(Collectors.toSet());

                for (Destroyable destroyable : destroyables) {
                    destroyable.destroy();
                }
            }

        }
    }
}