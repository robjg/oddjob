package org.oddjob.jmx;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oddjob.OjTestCase;

public class JMXAssumptionsTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(JMXAssumptionsTest.class);
	
	public class OurMBean extends NotificationBroadcasterSupport implements
	DynamicMBean {

		@Override
		public Object getAttribute(String attribute)
				throws AttributeNotFoundException, MBeanException,
				ReflectionException {
			throw new RuntimeException("Unexpected");
		}

		@Override
		public AttributeList getAttributes(String[] attributes) {
			throw new RuntimeException("Unexpected");
		}

		@Override
		public MBeanInfo getMBeanInfo() {
			return  new MBeanInfo(OurMBean.class.getName(), 
					"Test", 
					new MBeanAttributeInfo[0], 
					new MBeanConstructorInfo[0], 
					new MBeanOperationInfo[0], 
					new MBeanNotificationInfo[] {
				new MBeanNotificationInfo(new String[] { "test1" }, 
						Notification.class.getName(), "Test 1"), 
				new MBeanNotificationInfo(new String[] { "test2" }, 
						Notification.class.getName(), "Test 2") });
		}

		@Override
		public Object invoke(String actionName, Object[] params,
				String[] signature) throws MBeanException, ReflectionException {
			throw new RuntimeException("Unexpected");
		}

		@Override
		public void setAttribute(Attribute attribute)
				throws AttributeNotFoundException,
				InvalidAttributeValueException, MBeanException,
				ReflectionException {
			throw new RuntimeException("Unexpected");
		}

		@Override
		public AttributeList setAttributes(AttributeList attributes) {
			throw new RuntimeException("Unexpected");
		}		
	}
	
	class OurListener implements NotificationListener {

		Set<Thread> threads = new HashSet<Thread>();
		
		AtomicInteger count = new AtomicInteger();
		
		long last = -1;
		
		boolean badSequence;
		
		@Override
		public void handleNotification(Notification notification,
				Object handback) {
			synchronized (threads) {
				threads.add(Thread.currentThread());
			}
			logger.debug("Recevied notification " + notification.getSequenceNumber());
			if (notification.getSequenceNumber() != last + 1) {
				badSequence = true;
			}
			++last;			
			count.incrementAndGet();
		}
	
		public int getNumThreades() {
			synchronized (threads) {
				return threads.size();
			}
		}
	}
	
	
	
   @Test
	public void testStructuralNotificaitonAssumptions() throws Exception {
		
		
		JMXServiceURL address = new JMXServiceURL("service:jmx:rmi://");

		MBeanServer server = MBeanServerFactory.createMBeanServer();

		JMXConnectorServer cntorServer = JMXConnectorServerFactory.newJMXConnectorServer(
				address, null, server);
		cntorServer.start();
		
		final OurMBean ourMBean = new OurMBean();
		
		ObjectName name = new ObjectName("test", "test", "test");
		
		server.registerMBean(ourMBean, name);
		
		JMXConnector cntor = JMXConnectorFactory.connect(
				cntorServer.getAddress());

		MBeanServerConnection mbsc = cntor.getMBeanServerConnection();
	
		OurListener listener = new OurListener();
		
		mbsc.addNotificationListener(name, listener, null, null);
		
		
		ExecutorService executorService = Executors.newFixedThreadPool(5);
	
		for (int i = 0; i < 100; ++i) {
			final Notification n1 = new Notification("test1", name, i);
			final Notification n2 = new Notification("test2", name, i);
			Runnable r1 = new Runnable() {
				@Override
				public void run() {
					ourMBean.sendNotification(n1);
				}
			};
			Runnable r2 = new Runnable() {
				@Override
				public void run() {
					ourMBean.sendNotification(n2);
				}
			};
			executorService.submit(r1);
			executorService.submit(r2);
		}
		
		executorService.shutdown();
		assertTrue(executorService.awaitTermination(
				1000, TimeUnit.HOURS));
		
		while (listener.count.get() < 200) {
			synchronized (this) {
				wait(500);				
			}
		}
		
		assertEquals(1, listener.getNumThreades());
		
		cntor.close();
		cntorServer.stop();
	}
	
}
