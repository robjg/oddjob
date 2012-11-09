package org.oddjob.jmx;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.Structural;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryOwner;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jmx.general.Vendor;
import org.oddjob.rmi.RMIRegistryJob;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

public class JMXServiceJobTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(JMXServiceJobTest.class);

	ObjectName objectName;
	
	MBeanServer mBeanServer;

	JMXConnectorServer cntorServer;
	
	Vendor simple = new Vendor("Hay Medows");
	
	protected void createServer(Map<String, ?> environment) 
	throws Exception {
		
		
		RMIRegistryJob rmi = new RMIRegistryJob();
		rmi.setPort(13013);
		rmi.run();
		
		JMXServiceURL serviceURL = new JMXServiceURL(
				"service:jmx:rmi://ignored/jndi/rmi://localhost:13013/jmxrmi");
		objectName = new ObjectName("fruit:service=vendor,name=Pickles");
		
		mBeanServer = ManagementFactory.getPlatformMBeanServer();
		mBeanServer.registerMBean(simple, objectName);		
		
		cntorServer = JMXConnectorServerFactory.newJMXConnectorServer(
				serviceURL, environment, mBeanServer);
		
		cntorServer.start();
		String address = cntorServer.getAddress().toString();
		
		logger.info("Server started. Clients may connect to: " + address);
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
			
			// Check for bug where directory was set after children
			// created.
			if (event.getSource() instanceof BeanDirectoryOwner) {
				BeanDirectoryOwner directoryOwner = 
						(BeanDirectoryOwner) event.getSource();
				BeanDirectory directory = directoryOwner.provideBeanDirectory();
				if (directory == null) {
					throw new NullPointerException(
							"This is the bug - directory is null!!!");
				}
			}
			
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
	
	public void testExample() throws Exception {
		
		createServer(null);
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jmx/JMXServiceExample.xml", 
				getClass().getClassLoader()));
		
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Object test = lookup.lookup("jmx-service");
		
		// here to catch a bug with BeanDirectoryOwner
		ChildCatcher domainsCatcher = new ChildCatcher();

		((Structural) test).addStructuralListener(domainsCatcher);
		
		oddjob.run();
		
		assertEquals(ParentState.STARTED, 
				oddjob.lastStateEvent().getState());
		
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
		
		// Check domains 
		
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
		
		assertEquals(ParentState.STARTED, 
				oddjob.lastStateEvent().getState());
		
		// Check invoked
		assertEquals(94.23, lookup.lookup(
				"invoke-quote.result", double.class), 0.01);
		
		oddjob.stop();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());

		oddjob.destroy();
	}
	
	public void testHeartBeat() throws Exception {
		
		
		Map<String, Object> env = new HashMap<String, Object>();

		FailableSocketFactory ssf =  
			new FailableSocketFactory(); 
		
		env.put(RMIConnectorServer. 
				RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf); 
		
		createServer(env);
		
		JMXServiceJob client = new JMXServiceJob();
		client.setConnection("localhost:13013");
		client.setArooaSession(new StandardArooaSession());
		client.setHeartbeat(100);
		
		StateSteps clientStates = new StateSteps(client);
		clientStates.startCheck(ServiceState.READY, 
				ServiceState.STARTING, ServiceState.STARTED);
		
		client.run();
		
		clientStates.checkNow();
		
		clientStates.startCheck(ServiceState.STARTED, 
				ServiceState.EXCEPTION);
		
		Thread.sleep(400);
		
		logger.info("Setting scoket to fail!");
		
		ssf.setFail(true);
		
		clientStates.checkWait();
				
		ssf.setFail(false);
		
		clientStates.startCheck(ServiceState.EXCEPTION, 
				ServiceState.READY, 
				ServiceState.STARTING,
				ServiceState.STARTED);
		
		logger.debug("Client Running Again.");
		
		client.hardReset();
		
		client.run();
		
		clientStates.checkNow();
		
		client.stop();
	}
	
	public static void main(String... args) throws Exception {

		JMXServiceJobTest test = new JMXServiceJobTest();
		test.createServer(null);
		
		System.in.read();
		
		test.tearDown();
	}
}
