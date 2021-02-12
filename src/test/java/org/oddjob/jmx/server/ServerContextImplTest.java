/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.registry.*;
import org.oddjob.jmx.RemoteDirectory;
import org.oddjob.jmx.RemoteDirectoryOwner;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogListener;
import org.oddjob.util.MockThreadManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerContextImplTest {


    @Test
    public void testSimple() throws InvalidIdException {
        Object comp = new Object();
        SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
        cr1.register("foo", comp);

        ServerModel sm = new ServerModelImpl(
                new ServerId("//test"),
                new MockThreadManager(),
                new MockServerInterfaceManagerFactory());

        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(cr1);

        ServerContext test = new ServerContextImpl(
                comp, sm, parentContext);

        assertThat(test.getModel(), sameInstance(sm));

        assertThat(test.getAddress(),
                is(new Address(new ServerId("//test"), new Path("foo"))));
    }

    static class OurOwner extends MockBeanDirectoryOwner {
        BeanDirectory beanDirectory;

        public BeanDirectory provideBeanDirectory() {
            return beanDirectory;
        }
    }

    // registry when the top node is a registry owner.
    @Test
    public void testTopChildRegistry() throws ServerLoopBackException {
        OurOwner client = new OurOwner();

        SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
        cr1.register("client", client);

        SimpleBeanRegistry cr2 = new SimpleBeanRegistry();
        client.beanDirectory = cr2;


        Object node = new Object();
        cr2.register("foo", node);

        ServerModel sm = new ServerModelImpl(
                new ServerId("//test"),
                new MockThreadManager(),
                new MockServerInterfaceManagerFactory());

        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(cr1);

        ServerContext sc1 = new ServerContextImpl(
                client, sm, parentContext);

        Address address1 = new Address(
                new ServerId("//test"), new Path("client"));

        assertThat(sc1.getAddress(), is(address1));

        ServerContext sc2 = sc1.addChild(node);
        MatcherAssert.assertThat(sc1.getBeanDirectory(), not(sameInstance(sc2.getBeanDirectory())));

        Address address = new Address(
                new ServerId("//test"), new Path("client/foo"));

        assertThat(sc2.getAddress(), is(address));
    }

    // registry when the second node is a registry owner.
    @Test
    public void testChildRegistry() throws ServerLoopBackException {
        Object top = new Object();

        SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
        cr1.register("top", top);

        OurOwner node = new OurOwner();
        cr1.register("foo", node);

        SimpleBeanRegistry cr2 = new SimpleBeanRegistry();
        node.beanDirectory = cr2;

        ServerModel sm = new ServerModelImpl(
                new ServerId("//test"),
                new MockThreadManager(),
                new MockServerInterfaceManagerFactory());

        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(cr1);

        ServerContext sc1 = new ServerContextImpl(
                top, sm, parentContext);

        ServerContext sc2 = sc1.addChild(node);

        Object inner = new Object();
        cr2.register("inner", inner);
        // sc2 is only a parent when it needs to be
        ServerContext sc3 = sc2.addChild(inner);

        assertThat(sc3.getAddress(),
                is(new Address(new ServerId("//test"), new Path("foo/inner"))));
    }

    // registry when the second node is a registry owner.
    @Test
    public void testChildRegistryNoPath() throws ServerLoopBackException {
        SimpleBeanRegistry cr1 = new SimpleBeanRegistry();

        OurOwner top = new OurOwner();

        SimpleBeanRegistry cr2 = new SimpleBeanRegistry();
        top.beanDirectory = cr2;

        OurOwner node = new OurOwner();

        cr2.register("apples", node);

        SimpleBeanRegistry cr3 = new SimpleBeanRegistry();
        node.beanDirectory = cr3;

        Object inner = new Object();
        cr3.register("inner", inner);

        ServerModel sm = new ServerModelImpl(
                new ServerId("//test"),
                new MockThreadManager(),
                new MockServerInterfaceManagerFactory());

        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(cr1);

        ServerContext sc1 = new ServerContextImpl(
                top, sm, parentContext);

        ServerContext sc2 = sc1.addChild(node);

        ServerContext sc3 = sc2.addChild(inner);

        assertThat(sc3.getAddress(), nullValue());
    }

    static class OurRemote extends MockBeanDirectoryOwner
            implements RemoteDirectoryOwner {
        BeanDirectory beanDirectory;

        ServerId serverId;

        public RemoteDirectory provideBeanDirectory() {
            return new RemoteDirectory() {

                public ServerId getServerId() {
                    return serverId;
                }

                public <T> Iterable<T> getAllByType(Class<T> type) {
                    return beanDirectory.getAllByType(type);
                }

                public String getIdFor(Object bean) {
                    return beanDirectory.getIdFor(bean);
                }

                public Object lookup(String path) {
                    return beanDirectory.lookup(path);
                }

                public <T> T lookup(String path, Class<T> required)
                        throws ArooaConversionException {
                    return beanDirectory.lookup(path, required);
                }

            };
        }
    }

    @Test
    public void testDifferentServers() throws ServerLoopBackException {
        OurRemote top = new OurRemote();
        SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
        cr1.register("top", top);

        SimpleBeanRegistry cr2 = new SimpleBeanRegistry();
        top.serverId = new ServerId("//toast");
        top.beanDirectory = cr2;

        Object inner = new Object();
        cr2.register("inner", inner);

        ServerModel sm = new ServerModelImpl(
                new ServerId("//test"),
                new MockThreadManager(),
                new MockServerInterfaceManagerFactory());

        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(cr1);

        ServerContext sc1 = new ServerContextImpl(
                top, sm, parentContext);

        Address address1 = new Address(
                new ServerId("//test"), new Path("top"));
        assertThat(sc1.getAddress(), is(address1));

        ServerContext sc2 = sc1.addChild(inner);

        Address address2 = new Address(
                new ServerId("//toast"), new Path("inner"));
        assertThat(sc2.getAddress(), is(address2));
    }

    @Test
    public void testDuplicateServers() {
        OurRemote top = new OurRemote();
        SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
        cr1.register("top", top);

        SimpleBeanRegistry cr2 = new SimpleBeanRegistry();
        top.serverId = new ServerId("//test");
        top.beanDirectory = cr2;

        Object inner = new Object();
        cr2.register("inner", inner);

        ServerModel sm = new ServerModelImpl(
                new ServerId("//test"),
                new MockThreadManager(),
                new MockServerInterfaceManagerFactory());

        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(cr1);

        ServerContext sc1 = new ServerContextImpl(
                top, sm, parentContext);

        Address address1 = new Address(
                new ServerId("//test"), new Path("top"));
        assertThat(sc1.getAddress(), is(address1));

        try {
            sc1.addChild(inner);
            fail("Should fail.");
        } catch (ServerLoopBackException e) {
            // expected.
        }
    }

    @Test
    public void testLogArchiver() throws ServerLoopBackException {

        final Object node = new Object();

        class OurArchiver implements LogArchiver {
            public void addLogListener(LogListener l, Object component,
                                       LogLevel level, long last, int max) {
                throw new RuntimeException("Unexpected.");
            }

            public void removeLogListener(LogListener l, Object component) {
                throw new RuntimeException("Unexpected.");
            }
        }

        SimpleBeanRegistry cr1 = new SimpleBeanRegistry();

        OurArchiver top = new OurArchiver();
        cr1.register("top", top);

        cr1.register("foo", node);

        ServerModel sm = new ServerModelImpl(
                new ServerId("//test"),
                new MockThreadManager(),
                new MockServerInterfaceManagerFactory());

        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(cr1);

        ServerContext sc1 = new ServerContextImpl(top, sm, parentContext);

        ServerContext sc2 = sc1.addChild(node);

        assertThat(sc2.getLogArchiver(), is(top));
    }

    @Test
    public void testFindAncestor() throws ServerLoopBackException {

        BeanDirectory beanDirectory = mock(BeanDirectory.class);
        ServerModel model = mock(ServerModel.class);
        ServerContext parent = mock(ServerContext.class);
        when(parent.getBeanDirectory()).thenReturn(beanDirectory);

        ServerContextImpl a = new ServerContextImpl("Hello", model, parent);
        ServerContext b = a.addChild(2);
        ServerContext c = b.addChild(3);

        //noinspection OptionalGetWithoutIsPresent
        assertThat(c.findAncestorOfType(String.class).get(), is("Hello"));
    }
}
