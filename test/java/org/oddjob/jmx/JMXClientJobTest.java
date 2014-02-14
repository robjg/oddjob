/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.jmx;


import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.MockBeanDirectoryOwner;
import org.oddjob.arooa.registry.SimpleBeanRegistry;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jmx.client.ComponentTransportable;
import org.oddjob.jmx.server.OddjobMBeanFactory;
import org.oddjob.jobs.structural.ForEachJobTest;
import org.oddjob.jobs.structural.JobFolder;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogHelper;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.EventThreadLaterExecutor;
import org.oddjob.monitor.model.ExplorerContextFactory;
import org.oddjob.monitor.model.ExplorerModel;
import org.oddjob.monitor.model.JobTreeModel;
import org.oddjob.monitor.model.JobTreeNode;
import org.oddjob.monitor.model.MockExplorerContext;
import org.oddjob.monitor.model.MockExplorerModel;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;
import org.oddjob.tools.WaitForChildren;

/**
 * Test the JMXClientJob
 */
public class JMXClientJobTest extends TestCase {
	static final Logger logger = Logger.getLogger(JMXClientJobTest.class);
	
	/**
	 * Fixture
	 */
	public class ServerChild implements Structural {
		ChildHelper<Object> childHelper = new ChildHelper<Object>(this);
		String name;
		
		ServerChild(String name) {
			this.name = name;
		}
		public String toString() {
			return name;
		}
		public void addStructuralListener(StructuralListener listener) {
			childHelper.addStructuralListener(listener);
		}
		public void removeStructuralListener(StructuralListener listener) {
			childHelper.removeStructuralListener(listener);
		}
	}
	
	
	public void setUp() {
		logger.debug("================== Running " + getName() + "================");
//		System.setProperty("mx4j.log.priority", "trace");
	}

	private class OurSession extends StandardArooaSession {
		
		SimpleBeanRegistry registry = new SimpleBeanRegistry();
		
		@Override
		public BeanRegistry getBeanRegistry() {
			return registry;
		}
	}
	
	/**
	 * Fixture to create a server job.
	 * 
	 * @return A server job.
	 */
	JMXServerJob createServer() {
		OurSession session = new OurSession();
		
		ServerChild c1 = new ServerChild("test");
		session.registry.register("c1", c1);
		
		ServerChild test1 = new ServerChild("test1");
		session.registry.register("test1", test1);
		
		ServerChild test2 = new ServerChild("test2");
		session.registry.register("test2", test2);
		
		ServerChild test3 = new ServerChild("test3");
		session.registry.register("test3", test3);
		
		c1.childHelper.insertChild(0, test1);
		c1.childHelper.insertChild(1, test2);
		c1.childHelper.insertChild(2, test3);
		
		session.registry.register("dummy", new Object());
		
		
		JMXServerJob j = new JMXServerJob();
		j.setRoot(c1);
		j.setArooaSession(session);
				
		j.setUrl("service:jmx:rmi://");
		return j;
	}

	/**
	 * Thest running and stopping the client job.
	 *  
	 * @throws Exception
	 */
	public void testRun() throws Exception {
		
		JMXServerJob server = createServer();
		server.start();
		
		JMXClientJob client = new JMXClientJob();
		client.setArooaSession(new StandardArooaSession());
		client.setConnection(server.getAddress());
		client.run();
		
		Object[] children = OddjobTestHelper.getChildren(client);
		
		assertEquals("child", "test", children[0].toString());				
		
		Object[] children2 = OddjobTestHelper.getChildren((Structural) children[0]);
		
		assertEquals(3, children2.length);
		
		// got this far - must have worked.
		client.stop();				
		server.stop();
		
		assertEquals(ServiceState.STOPPED, client.lastStateEvent().getState());
	}

	/** 
	 * Tracking down a problem with the next test.
	 * @throws ArooaConversionException 
	 * @throws FailedToStopException 
	 */
	public void testPrinciplesOfNextTest() throws ArooaConversionException, FailedToStopException {
		
		String xml =
			"<oddjob id='this' xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" +
			"<job>" +
			" <jmx:server id='server' " +
			"         name='X'" +
			"         root='${server}' " +
			"         url='service:jmx:rmi://'/>" +
			"</job></oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		oj.run();
		
		String address = new OddjobLookup(oj).lookup(
				"server.address", String.class);
		assertNotNull(address);
		
		JMXClientJob client = new JMXClientJob();			
		client.setArooaSession(new StandardArooaSession());
		client.setConnection(address);
		
		client.run();

		Object[] children = OddjobTestHelper.getChildren(client);

		assertEquals(1, children.length);
		assertEquals("X", children[0].toString());
		
		client.stop();
		
		client.hardReset();
		
		client.run();
		
		children = OddjobTestHelper.getChildren(client);

		assertEquals(1, children.length);
		assertEquals("X", children[0].toString());
		
		client.destroy();
		
		oj.destroy();
	}
	
	/**
	 * This test creates a server and then attempts to
	 * connect and disconnect multiple times
	 */
	public void testRunLotsOfClients() throws Exception {

		String xml = 
			"<oddjob id='this' xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" +
			" <job>" +
			"  <jmx:server id='server' root='${this}' url='service:jmx:rmi://'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		oj.run();
		
		String address = new OddjobLookup(oj).lookup(
				"server.address", String.class);
		assertNotNull(address);
		
		Thread[] threads = new Thread[10];
		final boolean[] ok = new boolean[10];
		
		for (int i = 0; i < 10; i++) {
			logger.debug("************ " + i + " *************");
			
			final JMXClientJob client = new JMXClientJob();			
			client.setArooaSession(new StandardArooaSession());
			client.setConnection(address);
		
			final int index = i;
			
			Thread t2 = new Thread(
					new Runnable() {
						public void run() {
							client.run();
							WaitForChildren wait = new WaitForChildren(client);
							wait.waitFor(1);
							try {
								client.stop();
							} catch (FailedToStopException e) {
								throw new RuntimeException(e);
							}
							if (OddjobTestHelper.getJobState(client) == ServiceState.STOPPED) {
								ok[index] = true;
							}
						}
					});
			threads[i] = t2;
			t2.start();

		}

		for (int i = 0; i < 10; ++i) {
			threads[i].join();
		}
		
		logger.debug("stopping server");
		oj.stop();
		
		for (int i = 0; i < 10; ++i) {
			if (!ok[i]) {
				fail("" + i + "failed.");
			}
		}


	}
	
	// test looking up a property on a server using a path.
	public void testLookup() throws Exception {
		Oddjob server = new Oddjob();
		server.setConfiguration(
				new XMLConfiguration("Resource",
						this.getClass().getResourceAsStream("server1.xml")));
		server.run();
		
		
		String address = new OddjobLookup(server).lookup(
				"server.address", String.class);
		assertNotNull(address);
				
		Oddjob client = new Oddjob();
		client.setConfiguration(
				new XMLConfiguration("Resource", 
						this.getClass().getResourceAsStream("client1.xml")));
		client.setArgs(new String[] { address });

		client.run();
		client.stop();
		
		server.stop();
		
		assertEquals(ParentState.COMPLETE, client.lastStateEvent().getState());
		assertEquals(ParentState.COMPLETE, server.lastStateEvent().getState());
	}

	public static class Echo {
		Object echo;
		public Object getEchoWrapped() {
			logger.debug("returning [" + echo + "]");
				return new ComponentTransportable(
						OddjobMBeanFactory.objectName(1));
		}
		public void setEcho(Object echo) {
			logger.debug("setting [" + echo + "]");
			this.echo = echo;
		}
		
	}

	public void testHostRelative() throws Exception {
		OurSession serverSession = new OurSession();

		Echo e = new Echo();
		
		serverSession.registry.register("echo", e);		
		
		final JMXServerJob server = new JMXServerJob();
		server.setRoot(e);
		server.setArooaSession(serverSession);
				
		server.setUrl("service:jmx:rmi://");

		OurSession clientSession = new OurSession();
		
		JMXClientJob client = new JMXClientJob(); 
		clientSession.registry.register("client", client);
		client.setArooaSession(clientSession);

		server.start();
		
		client.setConnection(server.getAddress());
		client.run();
		
		DynaBean bean = (DynaBean) clientSession.registry.lookup("client/echo");
		assertNotNull(bean);
				
		bean.set("echo", bean);
				
		Object echo = bean.get("echoWrapped");
		assertEquals(bean, echo);
	
		client.stop();
		server.stop();

		assertEquals(ServiceState.STOPPED, client.lastStateEvent().getState());
	}

	class Owner extends MockBeanDirectoryOwner implements Structural {

		SimpleBeanRegistry beanRegistry = new SimpleBeanRegistry();

		ChildHelper<Object> helper = new ChildHelper<Object>(this);
		
		public BeanDirectory provideBeanDirectory() {
			return beanRegistry;
		}

		public String toString() {
			return "comp1";
		}
		
		public void addStructuralListener(StructuralListener listener) {
			helper.addStructuralListener(listener);
		}
		
		public void removeStructuralListener(StructuralListener listener) {
			helper.removeStructuralListener(listener);
		}
	}
	
	// test a remote nested registry.
	public void testRemoteNestedRegistry() throws Exception {
		OurSession serverSession = new OurSession();
		
		Owner comp1 = new Owner();
		serverSession.registry.register("comp1", comp1);
		
		Object comp2 = new Object() {
			@Override
			public String toString() {
				return "comp2";
			}
		};
		
		comp1.helper.insertChild(0, comp2);
		comp1.beanRegistry.register("comp2", comp2);
		
		JobFolder folder = new JobFolder();
		folder.setJobs(0, comp1);
		
		JMXServerJob server = new JMXServerJob();
		server.setRoot(folder);
		server.setArooaSession(serverSession);
				
		server.setUrl("service:jmx:rmi://");
		server.start();

		OurSession localSession = new OurSession();
		JMXClientJob client = new JMXClientJob(); 
		client.setArooaSession(localSession);
		
		client.setConnection(server.getAddress());
		client.run();
		
		// test we can look up a component in a nested registry.
		BeanDirectory mirrorCR1 = client.provideBeanDirectory();
		
		RemoteDirectoryOwner comp1Proxy = (RemoteDirectoryOwner) mirrorCR1.lookup("comp1");
		assertNotNull(comp1Proxy);
		assertEquals("comp1", comp1Proxy.toString());
		
		Object comp2Proxy = mirrorCR1.lookup("comp1/comp2");
		assertNotNull(comp2Proxy);
		assertEquals("comp2", comp2Proxy.toString());
				
		RemoteDirectory mirrorCR2 = comp1Proxy.provideBeanDirectory();
		assertNotNull(mirrorCR2);
		assertEquals(comp2Proxy, mirrorCR2.lookup("comp2"));

		client.stop();
		server.stop();
	}

	class ResultHolder {
		Object result;
	}

	/**
	 * When a client node such as Oddjob resets, check that the client
	 * removes it's registry.
	 */
	public void testRegistryManagement() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setName("Top OJ");
		oddjob.setConfiguration(
				new XMLConfiguration("Resource", 
						this.getClass().getResourceAsStream(
								"JMXClientJobTest1.xml")));
		oddjob.setExport("config-2",  
		        new ArooaObject(JMXClientJobTest.class.getResourceAsStream(
		        		"JMXClientJobTest2.xml")));
		oddjob.setExport("config-3", 
				new ArooaObject(JMXClientJobTest.class.getResourceAsStream(
						"JMXClientJobTest3.xml")));
		
		oddjob.run();
		
		JMXClientJob client = new JMXClientJob(); 
		client.setArooaSession(new StandardArooaSession());
		
		Object server = new OddjobLookup(oddjob).lookup("server");
		client.setConnection((String) PropertyUtils.getProperty(
				server, "address"));

		client.run();
		
		Object firstoj = new OddjobLookup(client).lookup("oj");
		assertNotNull(firstoj);
				
		Object seq = new OddjobLookup(client).lookup("oj/seq");
		assertNotNull(seq);
		
		Resetable nested = (Resetable) new OddjobLookup(client).lookup("oj/oj");
		assertNotNull(nested);

		assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(nested));

		Object echoJob = new OddjobLookup(client).lookup("oj/oj/fruit");
		assertNotNull(echoJob);
		
		StateSteps steps = new StateSteps((Stateful) nested);
		steps.startCheck(ParentState.COMPLETE, ParentState.READY);
		
		nested.hardReset();
		
		// takes a while for the notifications...
		steps.checkWait();
		
		assertNull(new OddjobLookup(client).lookup("oj/oj/fruit"));
		
		logger.info("Re-running Once...");
				
		steps.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.COMPLETE);
		
		((Runnable) nested).run();
		
		// We might still be waiting for notifications. This is where
		// we need to wait for a JobState.DESTROYED on echoJob.

		steps.checkWait();
		
		while (new OddjobLookup(client).lookup("oj/oj/fruit") == null) {
			Thread.sleep(1000);
			Thread.yield();
		}
							
		steps.startCheck(ParentState.COMPLETE, ParentState.READY);
		
		nested.hardReset();

		steps.checkWait();
		
		assertNull(new OddjobLookup(client).lookup("oj/oj/fruit"));
		
		logger.info("Re-running Twice...");
		
		steps.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.COMPLETE);
		
		((Runnable) nested).run();

		while (new OddjobLookup(client).lookup("oj/oj/fruit") == null) {
			Thread.sleep(1000);
			Thread.yield();
		}
		
		steps.checkWait();
		
		client.stop();
		((Stoppable) server).stop();
		
		oddjob.destroy();
	}

	/**
	 * Fixture for logger testing.
	 *
	 */
	public static class ThingWithLogger implements LogEnabled {
		public String loggerName() {
			return "org.oddjob.TestLogger";
		}
	}

	class MockLogListener implements LogListener {
		LogEvent e;
		synchronized public void logEvent(LogEvent logEvent) {
			this.e = logEvent;
			notifyAll();
		}
	}
	
	/**
	 * Test the client job as a log archiver.
	 *
	 */
	public void testLogArchiver() throws Exception {
		OurSession session = new OurSession();
		
		ThingWithLogger serverNode = new ThingWithLogger();
		session.registry.register("thing", serverNode);
		
		JMXServerJob server = new JMXServerJob();
		server.setArooaSession(session);
		server.setRoot(serverNode);
		server.setLogFormat("%m");		
		server.setUrl("service:jmx:rmi://");
		server.start();
		
		Logger ourLogger = Logger.getLogger(serverNode.loggerName());
		ourLogger.setLevel(Level.DEBUG);
		ourLogger.info("Test");
		
		JMXClientJob client = new JMXClientJob();
		client.setArooaSession(new StandardArooaSession());
		client.setConnection(server.getAddress());
		client.run();
		
		Object[] children = OddjobTestHelper.getChildren(client);
		
		Object proxy = children[0];

		assertEquals("logger name", "org.oddjob.TestLogger", 
				LogHelper.getLogger(proxy));
		
		MockLogListener ll = new MockLogListener();
		
		client.addLogListener(ll, proxy, LogLevel.DEBUG, -1, 10);
		
		// log poller runs on separate thread, so need to wait for event
		while (ll.e == null) {
			synchronized (ll) {
				ll.wait();
			}
		}
		assertNotNull("event", ll.e);
		assertEquals("message", "Test", ll.e.getMessage());
		
		client.stop();
		server.stop();
	}	
    
    // Make sure the bug fix doesn't leave Old Jobs lying around
    private static class JobCounter implements StructuralListener {
    	
    	private Set<Object> jobs = new HashSet<Object>();

		@Override
		public void childAdded(StructuralEvent event) {
			Object child = event.getChild();
			jobs.add(child);
			logger.info("JobCounter added " + child);
			if (child instanceof Structural) {
				((Structural) child).addStructuralListener(this);
			}
		}

		@Override
		public void childRemoved(StructuralEvent event) {
			Object child = event.getChild();
			jobs.remove(child);			
			logger.info("JobCounter removed " + child);
		}
    }
    
    // Ditto the JobTreeModel.
    private static class NodeCounter implements TreeModelListener {
    	
    	private Set<Object> jobs = new HashSet<Object>();

    	@Override
    	public void treeNodesChanged(TreeModelEvent e) {
    		// Don't care about Icon notifications.
    	}

    	@Override
    	public void treeStructureChanged(TreeModelEvent e) {
    		throw new RuntimeException("Unexpected!");
    	}

    	@Override
    	public synchronized void treeNodesInserted(TreeModelEvent e) {
    		assertEquals(1, e.getChildren().length);
    		JobTreeNode child = (JobTreeNode) e.getChildren()[0];
    		child.setVisible(true);
			jobs.add(child);
			logger.info("NodeCounter added " + child);
    	}

    	@Override
    	public synchronized void treeNodesRemoved(TreeModelEvent e) {
    		assertEquals(1, e.getChildren().length);
    		Object child = e.getChildren()[0];
			jobs.remove(child);
			logger.info("NodeCounter removed " + child);
		}
    }
    
    /**
     * Tracking down a bug where complicated structures cause an exception in explorer.
     * <p>
     * This is the flip side of fixing the bug {@link ForEachJobTest#testDestroyWithComplicateStructure()}
     * because fixing that caused a bug in the JMXClient.
     * 
     * @throws Exception 
     */
    public void testDestroyWithComplicatedStructure() throws Exception {
    	
    	String serverConfig = 
    		"<oddjob>" +
    		" <job>" +
    		"  <sequential>" +
    		"   <jobs>" +
    		"    <echo/>" +
    		"    <echo/>" +
    		"    <echo/>" +
    		"   </jobs>" +
    		"  </sequential>" +
    		" </job>" +
    		"</oddjob>";
    	
    	Oddjob serverOddjob = new Oddjob();
    	
    	serverOddjob.setConfiguration(new XMLConfiguration("XML" , serverConfig));
    
		JMXServerJob server = new JMXServerJob();
		server.setRoot(serverOddjob);
		server.setArooaSession(new OurSession());
				
		server.setUrl("service:jmx:rmi://");
		server.start();

    	serverOddjob.run();
    	
    	String clientConfig = 
    		"<oddjob>" +
    		" <job>" +
    		"  <jmx:client xmlns:jmx='http://rgordon.co.uk/oddjob/jmx' " + 
    		" id='client' connection='${server-address}'/>" +
    		" </job>" +
    		"</oddjob>";
    	
    	final Oddjob clientOddjob = new Oddjob();
    	
    	clientOddjob.setConfiguration(new XMLConfiguration("XML" , clientConfig));
    	clientOddjob.setExport("server-address", new ArooaObject(server.getAddress()));

		clientOddjob.run();
		assertEquals(ParentState.STARTED, clientOddjob.lastStateEvent().getState());
				    	    	
		JobTreeModel model = new JobTreeModel();

		NodeCounter nodeCounter = new NodeCounter();
		model.addTreeModelListener(nodeCounter);
		
		JobTreeNode root = new JobTreeNode(
				new MockExplorerModel() {
					@Override
					public Oddjob getOddjob() {
						return clientOddjob;
					}
				}, 
				model, 
				new EventThreadLaterExecutor(), 
				new ExplorerContextFactory() {
					@Override
					public ExplorerContext createFrom(ExplorerModel explorerModel) {
						return new MockExplorerContext() {
							@Override
							public ExplorerContext addChild(Object child) {
								return this;
							}
						};
					}
				});
		
		root.setVisible(true);
    	
    	JobCounter jobCounter = new JobCounter();
    	clientOddjob.addStructuralListener(jobCounter);
    	
    	SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				logger.info("Tree built and event queue should be clear.");
			}
		});
    	
		logger.info("Checking node counts.");
    	
    	assertEquals(6, jobCounter.jobs.size());    	
    	assertEquals(6, nodeCounter.jobs.size());

		logger.info("Stopping client.");
		JMXClientJob client = new OddjobLookup(
			clientOddjob).lookup("client", JMXClientJob.class);    	
    	client.stop();
    	
    	SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				logger.info("Tree destroyed and event queue should be clear.");
			}
		});
    	
    	// Only the top node is removed.
    	assertEquals(5, jobCounter.jobs.size());
    	// But all Job Nodes must be destroyed.
    	assertEquals(1, nodeCounter.jobs.size());
    	
    	serverOddjob.destroy();
    	server.stop();
    	clientOddjob.destroy();
    }
}
