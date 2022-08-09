/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.junit.Test;
import org.oddjob.Iconic;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.images.ImageData;
import org.oddjob.jmx.handlers.IconicHandlerFactory;
import org.oddjob.remote.Notification;
import org.oddjob.util.MockThreadManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IconicInfoTest extends OjTestCase {
//	private static final Logger logger = LoggerFactory.getLogger(IconicInfoTest.class);

    private static class OurHierarchicalRegistry extends MockBeanRegistry {

        @Override
        public String getIdFor(Object component) {
            assertNotNull(component);
            return "x";
        }
    }

    private static class OurServerSession extends MockServerSession {

        ArooaSession session = new StandardArooaSession();

        @Override
        public ArooaSession getArooaSession() {
            return session;
        }
    }

    private static class MyIconic implements Iconic {
        IconListener l;

        public void addIconListener(IconListener listener) {
            l = listener;
        }

        public ImageData iconForId(String id) {
            return IconHelper.completeIcon;
        }

        public void removeIconListener(IconListener listener) {
            l = null;
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIconForId() throws Exception {
        MyIconic iconic = new MyIconic();

        ServerInterfaceManagerFactoryImpl imf =
                new ServerInterfaceManagerFactoryImpl();

        imf.addServerHandlerFactories(
                new ServerInterfaceHandlerFactory[]{
                        new IconicHandlerFactory()});

        ServerModel sm = new ServerModelImpl(
                new ServerId("//test/"),
                new MockThreadManager(),
                imf);

        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(new OurHierarchicalRegistry());

        ServerContext serverContext = new ServerContextImpl(
                iconic, sm, parentContext);

        OddjobMBean ojmb = OddjobMBean.create(
                iconic, 0L,
                new OurServerSession(),
                serverContext);

        iconic.l.iconEvent(new IconEvent(iconic, "test"));

        Notification<IconicHandlerFactory.IconData> n =
                (Notification<IconicHandlerFactory.IconData>) ojmb.invoke(
                        "iconicSynchronize", new Object[]{},
                        new String[]{});

        assertEquals(IconicHandlerFactory.ICON_CHANGED_NOTIF_TYPE, n.getType());

        ImageData it = (ImageData) ojmb.invoke("Iconic.iconForId",
                new Object[]{"whatever"},
                new String[]{String.class.getName()});

        assertEquals("Complete", it.getDescription());
    }
}
