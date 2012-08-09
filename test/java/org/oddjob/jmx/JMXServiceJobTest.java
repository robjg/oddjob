package org.oddjob.jmx;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Structural;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jmx.general.Vendor;
import org.oddjob.rmi.RMIRegistryJob;
import org.oddjob.state.ParentState;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

public class JMXServiceJobTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(JMXServiceJobTest.class);

	ObjectName objectName;
	
	MBeanServer mBeanServer;

	JMXConnectorServer cntorServer;
	
	Vendor simple = new Vendor("Hay Medows");
	
	protected void setUp() throws Exception {
		
		
		RMIRegistryJob rmi = new RMIRegistryJob();
		rmi.setPort(13013);
		rmi.run();
		
		JMXServiceURL serviceURL = new JMXServiceURL(
				"service:jmx:rmi://ignored/jndi/rmi://localhost:13013/jmxrmi");
		objectName = new ObjectName("fruit:service=vendor,name=Pickles");
		
		mBeanServer = ManagementFactory.getPlatformMBeanServer();
		mBeanServer.registerMBean(simple, objectName);		
		
		cntorServer = JMXConnectorServerFactory.newJMXConnectorServer(
				serviceURL, null, mBeanServer);
		
		cntorServer.start();
		String address = cntorServer.getAddress().toString();
		
		logger.info("Server started. Clients may connect to: " + address);
		
//		synchronized(this) {
//			wait(0);
//		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		mBeanServer.unregisterMBean(objectName);
		cntorServer.stop();
	}
	
	private class ChildCatcher implements StructuralListener {
		final Map<String, Object> children = 
				new HashMap<String, Object>();
		
		public void childAdded(StructuralEvent event) {
			Object child = event.getChild();
			String name = child.toString();
			if (children.containsKey(name)) {
				throw new IllegalStateException();
			}
			children.put(name, child);
		}
		
		public void childRemoved(StructuralEvent event) {
			children.remove(event.getChild().toString());
		}

	}
	
	public void testExample() throws FailedToStopException, ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jmx/JMXServiceExample.xml", 
				getClass().getClassLoader()));
		oddjob.run();
		
		assertEquals(ParentState.ACTIVE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
				
		Object test = lookup.lookup("jmx-service");
		
		// Test Bean Directory

		String farm = lookup.lookup("echo-farm.text", String.class);
		assertEquals("Hay Medows", farm);

		// Check property set.
		assertEquals(4.2, lookup.lookup(
				"jmx-service/fruit:service=vendor,name=Pickles.Rating",
				double.class), 0.01);
		
		// Check invoked
		assertEquals(94.23, lookup.lookup(
				"invoke-quote.result", double.class), 0.01);
		
		ChildCatcher domainsCatcher = new ChildCatcher();

		((Structural) test).addStructuralListener(domainsCatcher);
		
		assertTrue(domainsCatcher.children.containsKey("fruit"));
				
		Structural fruitDomain = (Structural) 
				domainsCatcher.children.get("fruit");
		
		ChildCatcher fruitCatcher = new ChildCatcher();
		
		fruitDomain.addStructuralListener(fruitCatcher);
		
		assertTrue(fruitCatcher.children.size() == 1);
		
		
		oddjob.stop();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());

		
		assertEquals(0, fruitCatcher.children.size());
		assertEquals(0, domainsCatcher.children.size());
		
		// Do it all again
		
		Object sequential = lookup.lookup("sequential");
		((Resetable) sequential).hardReset();
		
		assertEquals(ParentState.READY, 
				((Stateful) sequential).lastStateEvent().getState());
		
		((Runnable) sequential).run();
		
		assertEquals(ParentState.ACTIVE, 
				oddjob.lastStateEvent().getState());
		
		// Check invoked
		assertEquals(94.23, lookup.lookup(
				"invoke-quote.result", double.class), 0.01);
		
		oddjob.stop();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());

		oddjob.destroy();
	}
}
