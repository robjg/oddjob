package org.oddjob.jmx.handlers;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.registry.*;
import org.oddjob.jmx.RemoteDirectory;
import org.oddjob.jmx.RemoteDirectoryOwner;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientSession;
import org.oddjob.jmx.client.MockClientSession;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.*;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

public class BeanDirectoryHandlerFactoryTest extends OjTestCase {

    static class ServerSideOwner1 extends MockBeanDirectoryOwner {

        public BeanDirectory provideBeanDirectory() {
            return null;
        }
    }

    static class OurServerToolkit1 extends MockServerSideToolkit {

        @Override
        public ServerContext getContext() {
            return new MockServerContext() {
                @Override
                public ServerId getServerId() {
                    return new ServerId("//Fish");
                }
            };
        }
    }

    static class OurClientToolkit extends MockClientSideToolkit {

        ServerInterfaceHandler handler;

        @SuppressWarnings("unchecked")
        @Override
        public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
                throws Throwable {
            return (T) handler.invoke(
                    remoteOperation,
                    args);
        }
    }

    @Test
    public void testGetServerId() {

        ServerSideOwner1 target = new ServerSideOwner1();

        BeanDirectoryHandlerFactory test = new BeanDirectoryHandlerFactory();

        ServerInterfaceHandler serverHandler = test.createServerHandler(
                target, new OurServerToolkit1());

        OurClientToolkit clientToolkit = new OurClientToolkit();
        clientToolkit.handler = serverHandler;

        RemoteDirectoryOwner client =
                new BeanDirectoryHandlerFactory.ClientFactory(
                ).createClientHandler(null, clientToolkit);

        RemoteDirectory remote = client.provideBeanDirectory();

        ServerId id = remote.getServerId();

        assertEquals("//Fish", id.toString());
    }

    static class ServerSideOwner2 extends MockBeanDirectoryOwner {

        String lookup;

        public BeanDirectory provideBeanDirectory() {
            return new MockBeanRegistry() {
                @Override
                public Object lookup(String path) {
                    lookup = path;
                    return "Fish";
                }

                @Override
                public <T> T lookup(String path, Class<T> required) {
                    return required.cast("Fish");
                }
            };
        }
    }

    static class OurServerToolkit2 extends MockServerSideToolkit {

        @Override
        public ServerSession getServerSession() {
            return new MockServerSession() {
                @Override
                public long nameFor(Object object) {
                    assertEquals("Fish", object);
                    return -1L;
                }
            };
        }
    }

    @Test
    public void testLookup() throws ArooaConversionException {

        ServerSideOwner2 target = new ServerSideOwner2();

        BeanDirectoryHandlerFactory test = new BeanDirectoryHandlerFactory();

        ServerInterfaceHandler serverHandler = test.createServerHandler(
                target, new OurServerToolkit2());

        OurClientToolkit clientToolkit = new OurClientToolkit();
        clientToolkit.handler = serverHandler;

        RemoteDirectoryOwner client =
                new BeanDirectoryHandlerFactory.ClientFactory(
                ).createClientHandler(null, clientToolkit);

        SimpleBeanRegistry registry = new SimpleBeanRegistry();

        registry.register("snacks", client);

        Object result = registry.lookup("snacks/and/this/goes/accross/the.wire");

        assertEquals("Fish", result);

        assertEquals("and/this/goes/accross/the.wire", target.lookup);

        String typedResult = registry.lookup("snacks/and/this/goes/accross/the.wire", String.class);

        assertEquals("Fish", typedResult);
    }

    static class ServerSideOwner3 extends MockBeanDirectoryOwner {

        SimpleBeanRegistry registry = new SimpleBeanRegistry();

        {
            registry.register("x", "Dog");
        }

        public BeanDirectory provideBeanDirectory() {
            return new MockBeanRegistry() {
                @Override
                public <T> Iterable<T> getAllByType(Class<T> type) {
                    return registry.getAllByType(type);
                }
            };
        }
    }

    long dogName = 2L;

    static class OurServerToolkit3 extends MockServerSideToolkit {

        @Override
        public ServerSession getServerSession() {
            return new MockServerSession() {
                @Override
                public long nameFor(Object object) {
                    assertEquals("Dog", object);
                    return 2L;
                }
            };
        }
    }

    class OurClientToolkit3 extends MockClientSideToolkit {

        ServerInterfaceHandler handler;

        @SuppressWarnings("unchecked")
        @Override
        public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
                throws Throwable {
            return (T) handler.invoke(
                    remoteOperation,
                    args);
        }

        @Override
        public ClientSession getClientSession() {
            return new MockClientSession() {
                @Override
                public Object create(long objectName) {
                    assertEquals(dogName, objectName);
                    return "Cat";
                }
            };
        }
    }

    @Test
    public void testGetAllByType() {

        ServerSideOwner3 target = new ServerSideOwner3();

        BeanDirectoryHandlerFactory test = new BeanDirectoryHandlerFactory();

        ServerInterfaceHandler serverHandler = test.createServerHandler(
                target, new OurServerToolkit3());

        OurClientToolkit3 clientToolkit = new OurClientToolkit3();
        clientToolkit.handler = serverHandler;

        RemoteDirectoryOwner client =
                new BeanDirectoryHandlerFactory.ClientFactory(
                ).createClientHandler(null, clientToolkit);

        Iterable<Object> iterable =
                client.provideBeanDirectory().getAllByType(Object.class);

        List<Object> results = new ArrayList<>();

        for (Object o : iterable) {
            results.add(o);
        }

        assertEquals(1, results.size());
        assertEquals("Cat", results.get(0));
    }

    @Test
    public void testEqualsAndHashCode() {

        BeanDirectoryHandlerFactory factory1 = new BeanDirectoryHandlerFactory();
        BeanDirectoryHandlerFactory factory2 = new BeanDirectoryHandlerFactory();

        assertThat(factory1.equals(factory2), is(true));
        assertThat(factory1.hashCode(), is(factory2.hashCode()));
    }
}
