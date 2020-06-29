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
import org.oddjob.jmx.handlers.IconicHandlerFactory;
import org.oddjob.remote.Notification;
import org.oddjob.util.MockThreadManager;

import javax.swing.*;

public class IconicInfoTest extends OjTestCase {
//	private static final Logger logger = LoggerFactory.getLogger(IconicInfoTest.class);
	
	private class OurHierarchicalRegistry extends MockBeanRegistry {
		
		@Override
		public String getIdFor(Object component) {
			assertNotNull(component);
			return "x";
		}
	}
	
	private class OurServerSession extends MockServerSession {
		
		ArooaSession session = new StandardArooaSession();
		
		@Override
		public ArooaSession getArooaSession() {
			return session;
		}
	}
		
	private class MyIconic implements Iconic {
		IconListener l;
		public void addIconListener(IconListener listener) {
			l = listener;
		}
		public ImageIcon iconForId(String id) {
			return IconHelper.completeIcon;
		}
		public void removeIconListener(IconListener listener) {
			l = null;
		}
	}
	
   @Test
	public void testIconForId() throws Exception {
		MyIconic iconic = new MyIconic();
		
		ServerInterfaceManagerFactoryImpl imf = 
			new ServerInterfaceManagerFactoryImpl();

		imf.addServerHandlerFactories(
				new ServerInterfaceHandlerFactory[] { 
						new IconicHandlerFactory() });
		
		ServerModel sm = new ServerModelImpl(
				new ServerId("//test/"), 
				new MockThreadManager(), 
				imf);
		
		ServerContext serverContext = new ServerContextImpl(
				iconic, sm, new OurHierarchicalRegistry()); 
		
		OddjobMBean ojmb = new OddjobMBean(
				iconic, OddjobMBeanFactory.objectName(0),
				new OurServerSession(), 
				serverContext);
		
		iconic.l.iconEvent(new IconEvent(iconic, "test"));
		
		Notification[] notifications = (Notification[]) ojmb.invoke(
				"iconicSynchronize", new Object[] { }, 
				new String[] { } );
		
		Notification n = notifications[0];
		
		assertEquals(IconicHandlerFactory.ICON_CHANGED_NOTIF_TYPE, n.getType());

		ImageIcon it = (ImageIcon) ojmb.invoke("Iconic.iconForId", new Object[] { "whatever" }, 
				new String[] { String.class.getName() } );
		
		assertEquals("Complete", it.getDescription());
	}
}
