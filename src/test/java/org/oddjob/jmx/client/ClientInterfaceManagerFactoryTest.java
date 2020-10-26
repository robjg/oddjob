package org.oddjob.jmx.client;

import org.junit.Test;
import org.mockito.Mockito;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.life.ClassLoaderClassResolver;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.handlers.ObjectInterfaceHandlerFactory;
import org.oddjob.remote.Implementation;
import org.oddjob.remote.Initialisation;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientInterfaceManagerFactoryTest extends OjTestCase {

    interface Foo {
        void foo();
    }


    @Test
    public void testPrepareAndInvoke() throws Throwable {

        class MockFoo implements Foo {
            boolean invoked;

            public void foo() {
                invoked = true;
            }
        }
        MockFoo foo = new MockFoo();

        class FooClientHandlerFactory extends MockClientInterfaceHandlerFactory<Foo> {

            @Override
            public Foo createClientHandler(Foo proxy, ClientSideToolkit toolkit) {
                return proxy;
            }

            @Override
            public Class<Foo> interfaceClass() {
                return Foo.class;
            }

        }

        Implementation<?>[] implementations = new Implementation[] {
                Implementation.create(Foo.class.getName(), "2.0") };

        ClientInterfaceManagerFactory.Prepared test = new ClientInterfaceManagerFactoryBuilder()
                .addFactory(new FooClientHandlerFactory())
                .build()
                .prepare(implementations, new ClassLoaderClassResolver(getClass().getClassLoader()));

        assertThat(test.supportedInterfaces(), is(new Class<?>[]{Foo.class}));

        ClientInterfaceManager cim = test.create(foo, null);

        cim.invoke(Foo.class.getMethod("foo", (Class<?>[]) null), null);

        assertTrue(foo.invoked);
    }

    /**
     * Test that it fails with no factory.
     *
     * @throws Throwable
     */
    @Test
    public void testNoFactory() throws Throwable {

        class MockFoo implements Foo {
            boolean invoked;

            public void foo() {
                invoked = true;
            }
        }
        MockFoo foo = new MockFoo();

        Implementation<?>[] implementations = new Implementation[]{
                Implementation.create(Foo.class.getName(), "2.0")
        };

        ClientInterfaceManagerFactory.Prepared test = new ClientInterfaceManagerFactoryBuilder()
                .build()
                .prepare(implementations, new ClassLoaderClassResolver(getClass().getClassLoader()));

        ClientInterfaceManager cim = test.create(foo, null);
        try {
            cim.invoke(Foo.class.getMethod("foo", (Class<?>[]) null), null);
            fail("No interface factory so should fail");
        } catch (IllegalArgumentException e) {
            // expected
        }

        assertFalse(foo.invoked);
    }

    /**
     * Test for Object
     *
     * @throws Throwable
     */
    @Test
    public void testForObject() throws Throwable {

        class MockFoo implements Foo {
            public void foo() {
            }
        }

        MockFoo foo = new MockFoo();

        class FooClientHandlerFactory extends MockClientInterfaceHandlerFactory<Foo> {
            public Foo createClientHandler(Foo proxy, ClientSideToolkit toolkit) {
                return proxy;
            }

            public Class<Foo> interfaceClass() {
                return Foo.class;
            }

        }

        class OClientHandlerFactory extends MockClientInterfaceHandlerFactory<Object> {
            public Object createClientHandler(final Object proxy,
                                              ClientSideToolkit toolkit) {
                return new Object() {
                    public String toString() {
                        return "Test";
                    }
                };
            }

            public Class<Object> interfaceClass() {
                return Object.class;
            }

        }

        Implementation<?>[] implementations = new Implementation[]{
                Implementation.create(Object.class.getName(), "2.0"),
                Implementation.create(Foo.class.getName(), "2.0")
        };

        ClientInterfaceManagerFactory.Prepared test = new ClientInterfaceManagerFactoryBuilder()
                .addFactories(new OClientHandlerFactory(), new FooClientHandlerFactory())
                .build()
                .prepare(implementations, new ClassLoaderClassResolver(getClass().getClassLoader()));

        ClientInterfaceManager cim = test.create(foo, null);

        Class<?>[] supported = test.supportedInterfaces();

        assertThat(supported, is(new Class<?>[] { Foo.class } ));

        Object result = cim.invoke(Object.class.getMethod("toString", (Class<?>[]) null), null);

        assertEquals("Test", result);
    }

    @Test
    public void whenMethodRegisteredTwiceThenExceptionThrown() {

        @SuppressWarnings("unchecked")
        ClientInterfaceHandlerFactory<Runnable> handlerFactory =
                mock(ClientInterfaceHandlerFactory.class);

        when(handlerFactory.interfaceClass()).thenReturn(Runnable.class);

        try {
            new ClientInterfaceManagerFactoryBuilder()
                    .addFactories(handlerFactory, handlerFactory)
                    .build();
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            // expected.
        }
    }

    @Test
    public void testWithSomeRealHandlerFactories() throws Throwable {

        Implementation<?>[] implementations = new Implementation[]{
                Implementation.create(Object.class.getName(), "2.0",
                        new Initialisation<>(String.class, "Foo")),
                Implementation.create(RemoteOddjobBean.class.getName(), "2.0")
        };

        ClientInterfaceManagerFactory.Prepared test =
                new ClientInterfaceManagerFactoryBuilder()
                        .addFactories(
                                new ObjectInterfaceHandlerFactory.ClientFactory(),
                                new DirectInvocationClientFactory<>(RemoteOddjobBean.class))
                        .build()
                        .prepare(implementations, new ClassLoaderClassResolver(getClass().getClassLoader()));

        Method toString = Object.class.getMethod("toString");

        ClientSideToolkit toolkit = Mockito.mock(ClientSideToolkit.class);

        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(),
                test.supportedInterfaces(), (proxy1, method, args) -> {
            throw new RuntimeException("Unexpected");
                });

        ClientInterfaceManager cim = test.create(proxy, toolkit);

        String result = (String) cim.invoke(toString, new Object[0]);

        assertThat(result, is("Foo"));

        Mockito.verifyNoMoreInteractions(toolkit);
    }
}
