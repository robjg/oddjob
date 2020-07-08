package org.oddjob.jmx.client;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.oddjob.OjTestCase;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.handlers.ObjectInterfaceHandlerFactory;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientInterfaceManagerFactoryTest extends OjTestCase {

    interface Foo {
        void foo();
    }

    @Test
    public void testInvoke() throws Throwable {

        class MockFoo implements Foo {
            boolean invoked;

            public void foo() {
                invoked = true;
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

        ClientInterfaceManagerFactory test = new ClientInterfaceManagerFactoryBuilder()
                .addFactory(new FooClientHandlerFactory())
                .build();

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

        ClientInterfaceManagerFactory test = new ClientInterfaceManagerFactoryBuilder()
                .build();

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

        ClientInterfaceManagerFactory test = new ClientInterfaceManagerFactoryBuilder()
                .addFactories(new OClientHandlerFactory(), new FooClientHandlerFactory())
                .build();

        ClientInterfaceManager cim = test.create(foo, null);

        Class<?>[] supported = test.filter(new Class<?>[]{Object.class, Foo.class});

        assertThat(supported, Matchers.is(new Class<?>[]{Foo.class}));

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

        ClientInterfaceManagerFactory test =
                new ClientInterfaceManagerFactoryBuilder()
                        .addFactories(
                                new ObjectInterfaceHandlerFactory.ClientFactory(),
                                new DirectInvocationClientFactory<>(RemoteOddjobBean.class))
                        .build();

        Method toString = Object.class.getMethod("toString");

        ClientSideToolkit toolkit = Mockito.mock(ClientSideToolkit.class);
        Mockito.when(toolkit.invoke(MethodOperation.from(toString)))
                .thenReturn("Foo");

        ClientInterfaceManager cim = test.create(new Object(), toolkit);

        String result = (String) cim.invoke(toString, new Object[0]);

        assertThat(result, Matchers.is("Foo"));
    }
}
