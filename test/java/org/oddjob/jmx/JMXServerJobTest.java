/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.jmx;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.SimpleBeanRegistry;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.WaitJob;
import org.oddjob.state.IsNot;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.state.State;
import org.oddjob.state.StateConditions;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;
import org.oddjob.tools.WaitForChildren;

/**
 *
 */
public class JMXServerJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(JMXServerJobTest.class);
	
	protected void setUp() {
		logger.debug("================= " + getName() + " ==================");
	}
	
	int unique;
	
	Map<Object, String> ids = new HashMap<Object, String>();
	
	private class OurEmptyRegistrySession extends StandardArooaSession {

		@Override
		public BeanRegistry getBeanRegistry() {
			return new MockBeanRegistry() {
				@Override
				public String getIdFor(Object component) {
					assertNotNull(component);
					String id = ids.get(component);
					if (id == null) {
						id = "x" + unique++;
						ids.put(component, id);
					}
					return id;
				}
			};
		}
		
	} 
	
	public void testServerMBeans() throws Exception {
		
		Object root = new Object() {
			public String toString() {
				return "test";
			}
		};
		
		JMXServerJob server = new JMXServerJob();
		server.setRoot(root);
		server.setArooaSession(new StandardArooaSession());
		server.setUrl("service:jmx:rmi://");
		
		server.start();

		JMXServiceURL address = new JMXServiceURL(server.getAddress());

		JMXConnector cntor = JMXConnectorFactory.connect(address);

		MBeanServerConnection mBeanServer = cntor.getMBeanServerConnection();

		assertEquals(new Integer(3), mBeanServer.getMBeanCount());
		
		cntor.close();
		
		server.stop();
		
	}
	
	/** Test the Server job starts runs OK. */
	public void testRun() throws Exception {
		Object root = new Object() {
			public String toString() {
				return "test";
			}
		};
		
		JMXServerJob server = new JMXServerJob();
		server.setRoot(root);
		server.setArooaSession(new StandardArooaSession());
		server.setUrl("service:jmx:rmi://");
		
		server.start();
		
		JMXClientJob client = new JMXClientJob();
		client.setConnection(server.getAddress());
		client.setArooaSession(new StandardArooaSession());
	
		client.run();
		
		Object[] children = OddjobTestHelper.getChildren(client);
		
		assertEquals(1, children.length);
		assertEquals("test", children[0].toString()); 
		
		client.stop();
		
		server.stop();
	}
	
	public static class Component {
		public String getFruit() {
			return "apples";
		}
	}
	
	private class OurSession extends StandardArooaSession {
		SimpleBeanRegistry registry = new SimpleBeanRegistry();
		
		@Override
		public BeanRegistry getBeanRegistry() {
			return registry;
		}		
	}
	
	/** Test a nested client. */
	public void testLinkedServers() throws Exception {
		
		// server2
		ArooaSession server2Session = new OurSession();
		
		Component comp1 = new Component();
		server2Session.getBeanRegistry().register("fred", comp1);
		
		
		JMXServerJob server2 = new JMXServerJob();
		server2.setRoot(comp1);
		server2.setArooaSession(server2Session);
		server2.setUrl("service:jmx:rmi://");
		server2.start();
		
		// server 1 and client context
		OurSession server1Session = new OurSession();

		// client
		JMXClientJob client = new JMXClientJob();
		server1Session.registry.register("client", client);
		client.setArooaSession(server1Session);
		client.setConnection(server2.getAddress());
		client.run();

		// server1
		JMXServerJob server1 = new JMXServerJob();
		
		server1.setRoot(client);		
		server1.setUrl("service:jmx:rmi://");
		server1.setArooaSession(new OurEmptyRegistrySession());
		server1.start();
		
		Object o = server1Session.registry.lookup("client/fred");
		assertNotNull(o);
		DynaBean db = (DynaBean) o;
		assertEquals("apples", db.get("fruit"));
		
		client.stop();
		server1.stop();
		server2.stop();		
	}
	
	/** Test a nested oddjob. 
	 * The sever exports and oddjob which has a node. If the oddjob has id
	 * 'oj' and the node has id 'test' the client should be able to look
	 * up oj/test
	 * */
	public void testNestedOddjob() throws Exception {

		String EOL = System.getProperty("line.separator");
		
		final String xml = 
			"<oddjob xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" + EOL + 
			" <job>" + EOL +
			"  <sequential>" + EOL +
			"   <jobs>" + EOL +
			"    <jmx:server id='server' root='${oj}' url='service:jmx:rmi://'/>" + EOL +
			"    <oddjob id='oj'>" + EOL +
			"     <configuration>" + EOL +
			"        <xml>" + EOL +
			"         <oddjob>" + EOL +
			"          <job>" + EOL +
			"           <echo name='Test' id='echo'>Hello</echo>" + EOL +
			"          </job>" + EOL +
			"         </oddjob>" + EOL +
			"        </xml>" + EOL +
			"     </configuration>" + EOL +
			"    </oddjob>" + EOL +
			"   </jobs>" + EOL +
			"  </sequential>" + EOL +
			" </job>" + EOL +
			"</oddjob>" + EOL;
		
		final Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		oj.run();
		
//		OddjobExplorer explorer = new OddjobExplorer();
//		explorer.setOddjob(oj);
//		explorer.run();

		// sanity check what we'll find via the client.
		assertNotNull(new OddjobLookup(oj).lookup("oj/echo"));
		
		// client
		JMXClientJob client = new JMXClientJob();
		client.setArooaSession(new StandardArooaSession());
		client.setConnection((String) new OddjobLookup(oj).lookup("server.address"));
		client.run();

		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		
		oj.run();
				
		Object o = new OddjobLookup(client).lookup("oj");
		assertNotNull(o);
				
		// will takes time to process child added notifications.
		while (new OddjobLookup(client).lookup("oj/echo") == null) {
			try {
				Thread.sleep(10000);
			} catch (Exception e) {}
			Thread.yield();
		}
				
		// check we found the right thing.
		assertNotNull("Test", new OddjobLookup(client).lookup("oj/echo").toString());
		
		client.stop();
		oj.stop();
		
	}

	private class MyFolder implements Structural {
		final int level;
		final int number;
		final BeanRegistry registry;
		
		MyFolder(int number, int level, BeanRegistry registry) {
			this.number = number;
			this.level = level;
			this.registry = registry;
			
			registry.register("x" + unique++, this);
		}
		
		ChildHelper<MyFolder> childHelper = new ChildHelper<MyFolder>(this);
		
		public void addStructuralListener(StructuralListener listener) {
			childHelper.addStructuralListener(listener);
		}
		public void removeStructuralListener(StructuralListener listener) {
			childHelper.removeStructuralListener(listener);
			
		}
		private void addChildren(final int number, final int levels, final int level) {
			if (levels == 0) {
				return;
			}
			for (int i = 0; i < number; ++i) {
				final MyFolder child = new MyFolder(i, level, registry);
				childHelper.insertChild(i, child);
				child.addChildren(number, levels - 1, level + 1);
			}
		}
		
		void addChildren(int number, int levels) {
			addChildren(number, levels, level);
		}
		
		void removeChildren() {
			for (MyFolder child : childHelper.getChildren(new MyFolder[0])) {
				child.removeChildren();
			}
			childHelper.removeAllChildren();
		}
		public String toString() {
			return ("[" + number + ", " + level + "]");
		}
	}
	
	/** Big Structural Test. */
	public void testLotsOfStructural() throws Exception {
		
		OurSession session = new OurSession();
		
		MyFolder folder = new MyFolder(0, 0, session.registry);
		
		JMXServerJob server = new JMXServerJob();
		server.setRoot(folder);
		server.setUrl("service:jmx:rmi://");
		server.setArooaSession(session);
		server.start();

		// client
		JMXClientJob client = new JMXClientJob();
		client.setArooaSession(new StandardArooaSession());
		client.setConnection(server.getAddress());
		client.run();

		Object proxy = ChildHelper.getChildren(client)[0];
				
		folder.addChildren(5, 3);
		
		WaitForChildren w = new WaitForChildren(proxy);
		w.waitFor(5);
		
		folder.removeChildren();

		w.waitFor(0);
		
		client.stop();
		server.stop();		
	}
	
	/** Test destroying server sets client to incomplete. */
	public void testDestroyServer() throws Exception {
		
		OurSession session = new OurSession();
		
		MyFolder folder = new MyFolder(0, 0, session.registry);		
		folder.addChildren(3, 2);
				
		logger.info("**** Starting Server ****");
		
		JMXServerJob server = new JMXServerJob();
		server.setRoot(folder);
		server.setUrl("service:jmx:rmi://");
		server.setArooaSession(session);
		server.start();
		
		logger.info("**** Starting Client ****");
		
		// client
		JMXClientJob client = new JMXClientJob();
		client.setArooaSession(new StandardArooaSession());
		client.setConnection(server.getAddress());
		client.run();
		
		Object proxy = ChildHelper.getChildren(client)[0];
				
		WaitForChildren w = new WaitForChildren(proxy);
		w.waitFor(3);
		
		logger.info("**** Stopping Server ****");
		
		server.stop();

		logger.info("**** Waiting For Client to Stop ****");
		
		WaitJob wj = new WaitJob();
		wj.setState(new IsNot(StateConditions.RUNNING));
		wj.setFor(client);
		wj.run();

		State last = client.lastStateEvent().getState();
		logger.info("Client State: " + last);
		
		if (last.isException()) {
			Throwable e = client.lastStateEvent().getException();
			if ("Server Stopped.".equals(e.getMessage())) {
				logger.info(e.getMessage());
			}
			else {
				// Heart beat failure could happen first. Just log this.
				// would be nice to guarantee that it didn't but I can't
				// work that out.
				logger.info("Client Exception is:", e);
			}
		}
		else {
			fail("Unexpected state: " + 
					client.lastStateEvent().getState());
		}
	}
	
	// test for a bug where the server didn't clear down it's registry so
	// bouncing a nested Oddjob caused an 'component with that id already exists'
	// exception.
	public void testBounceOddjob() throws Exception {
		
		String EOL = System.getProperty("line.separator");
		
		final String xml = 
			"<oddjob xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" + EOL +
			" <job>" + EOL +
			"  <sequential>" + EOL +
			"   <jobs>" + EOL +
			"    <jmx:server id='server' root='${oj}'" +
			"            url='service:jmx:rmi://'/>" + EOL +
			"    <oddjob id='oj'>" + EOL +
			"     <configuration>" + EOL +
			"        <xml>" + EOL +
			"         <oddjob>" + EOL +
			"          <job>" + EOL +
			"           <echo name='Test' id='echo'>Hello</echo>" + EOL +
			"          </job>" + EOL +
			"         </oddjob>" + EOL +
			"        </xml>" + EOL +
			"     </configuration>" + EOL +
			"    </oddjob>" + EOL +
			"   </jobs>" + EOL +
			"  </sequential>" + EOL +
			" </job>" + EOL +
			"</oddjob>" + EOL;
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
		
		Oddjob innerOddjob = (Oddjob) new OddjobLookup(oj).lookup("oj");
		innerOddjob.hardReset();

		innerOddjob.run();
		
		oj.stop();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
	}
	
	/**
	 * Tracking down a problem where the server doesn't stop when oddjob
	 * is destroyed.
	 */
	public void testServerCopesWhenItsOddjobIsDestroyed() throws Exception {
		
		File file = new File(getClass().getResource(
				"JMXServerJobDestroyTest.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		oddjob.run();
		
		assertEquals(ParentState.ACTIVE, 
				oddjob.lastStateEvent().getState());
		
		Stateful server = new OddjobLookup(oddjob).lookup("server", 
				Stateful.class);
		
		StateSteps serverStates = new StateSteps(server);
		serverStates.startCheck(ServiceState.STARTED, ServiceState.STOPPED,
				ServiceState.DESTROYED);
		
		oddjob.destroy();
		
		assertEquals(ParentState.DESTROYED, 
				oddjob.lastStateEvent().getState());
		
		serverStates.checkNow();
	}
	
}
