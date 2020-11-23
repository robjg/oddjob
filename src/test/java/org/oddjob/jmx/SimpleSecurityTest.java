package org.oddjob.jmx;

import org.apache.commons.beanutils.DynaBean;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.client.UsernamePassword;
import org.oddjob.jmx.server.SimpleServerSecurity;
import org.oddjob.jobs.EchoJob;
import org.oddjob.rmi.RMIRegistryJob;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.FragmentHelper;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.DestroyFailedException;
import java.io.File;

/**
 * For these tests to work you need to download  
 * <a href="http://download.oracle.com/javase/1.5.0/docs/guide/jmx/examples/jmx_examples.zip">Zip file of all the JMX technology examples</a>
 * and set the oddjob.jmx.test.security.config property to point to 
 * the Security/simple/config directory.
 * 
 * @author rob
 *
 */
public class SimpleSecurityTest extends OjTestCase {

	private static final Logger logger = LoggerFactory.getLogger(SimpleSecurityTest.class);
	
	static final String SECURITY_CONFIG_PARAM = "oddjob.jmx.test.security.config";
	
   @Before
   public void setUp() throws Exception {

		logger.debug("------------------- " + getName() + " --------------");
	}
	
	boolean useSSL;
	
   @Test
	public void testSimpleOKExample() throws Exception {
	
		String param = System.getProperty(SECURITY_CONFIG_PARAM);
		if (param == null) {
			logger.info(SECURITY_CONFIG_PARAM + " not defined.");
			return;
		}
		else {
			logger.info(SECURITY_CONFIG_PARAM + "=" + param);
		}
		
		File config = new File(param);
		
		assertTrue(config.exists());
		
		Object root = new Object() {
			public String toString() {
				return "test";
			}
		};
		
		SimpleServerSecurity security = new SimpleServerSecurity();
		security.setPasswordFile(new File(config, "password.properties"));
		security.setAccessFile(new File(config, "access.properties"));
		security.setUseSSL(useSSL);
		
		JMXServerJob server = new JMXServerJob();
		server.setRoot(root);
		server.setArooaSession(new StandardArooaSession());
		server.setUrl("service:jmx:rmi://");
		server.setEnvironment(security.toValue());
		
		server.start();
		
		UsernamePassword credentials = new UsernamePassword();
		credentials.setUsername("username");
		credentials.setPassword("password");
		
		JMXClientJob client = new JMXClientJob();
		client.setConnection(server.getAddress());
		client.setArooaSession(new StandardArooaSession());
		client.setEnvironment(credentials.toValue());		
		client.run();
		
		Object[] children = OddjobTestHelper.getChildren(client);
		
		assertEquals(1, children.length);
		assertEquals("test", children[0].toString()); 
		
		client.stop();
		
		server.stop();
	}
	
   @Test
	public void testSslOKExample() throws Exception {
		
		String param = System.getProperty("oddjob.jmx.test.security.config");
		if (param == null) {
			return;
		}
		
		useSSL = true;
		
		System.setProperty("javax.net.ssl.keyStore",
				param + "/keystore");
		System.setProperty("javax.net.ssl.keyStorePassword",
				"password"); 

		System.setProperty("javax.net.ssl.trustStore",
				param + "/truststore"); 
		System.setProperty("javax.net.ssl.trustStorePassword",
				"trustword");

		testSimpleOKExample();
	}
	
   @Test
	public void testReadonlyAccess() throws Exception {

		try (OddjobConsole.Close close = OddjobConsole.initialise()) {
			
			OurDirs dirs = new OurDirs();
	
			File config = dirs.relative("test/jmx");
			
			assertTrue(config.exists());
			
			EchoJob echo = new EchoJob();
			echo.setText("Hello World");
			
			Object root = new OddjobComponentResolver(
					).resolve(echo, null);
			
			SimpleServerSecurity security = new SimpleServerSecurity();
			security.setPasswordFile(new File(config, "password.properties"));
			security.setAccessFile(new File(config, "access.properties"));
			security.setUseSSL(useSSL);
			
			JMXServerJob server = new JMXServerJob();
			server.setRoot(root);
			server.setArooaSession(new StandardArooaSession());
			server.setUrl("service:jmx:rmi://");
			server.setEnvironment(security.toValue());
			
			server.start();
			
			UsernamePassword credentials = new UsernamePassword();
			credentials.setUsername("rod");
			credentials.setPassword("rainbow1");
			
			JMXClientJob client = new JMXClientJob();
			client.setConnection(server.getAddress());
			client.setArooaSession(new StandardArooaSession());
			client.setEnvironment(credentials.toValue());		
			client.run();
			
			Object[] children = OddjobTestHelper.getChildren(client);
			
			assertEquals(1, children.length);
			
			Object child = children[0];
			
			assertEquals("Echo", child.toString()); 
			
			assertEquals(false, child instanceof Runnable);
			assertEquals(true, child instanceof Stateful);
			assertEquals(true, child instanceof Iconic);
			assertEquals(false, child instanceof Resettable);
			assertEquals(false, child instanceof DynaBean);
			assertEquals(true, child instanceof RemoteOddjobBean);
					
			client.stop();
			server.stop();
		}
	}
	
   @Test
	public void testClientAndServerExamples() throws ArooaParseException, DestroyFailedException, FailedToStopException {
		
		FragmentHelper helper = new FragmentHelper();
		
		Object server = helper.createComponentFromResource(
				"org/oddjob/jmx/SecureServerExample.xml");
		helper.getSession().getBeanRegistry().register("some-job", 
				new Object());
			
		JMXClientJob client = (JMXClientJob) helper.createComponentFromResource(
				"org/oddjob/jmx/SecureClientExample.xml");
		
		
		String param = System.getProperty("oddjob.jmx.test.security.config");
		if (param == null) {
			return;
		}	
		
		RMIRegistryJob registry = new RMIRegistryJob();
		registry.run();
		
		StateSteps serverStates = new StateSteps((Stateful) server);
		serverStates.startCheck(ServiceState.STOPPED, 
				ServiceState.STARTING, ServiceState.STARTED);
		
		((Runnable) server).run();
		
		serverStates.checkNow();
		
		StateSteps clientStates = new StateSteps(client);
		clientStates.startCheck(ServiceState.STOPPED, 
				ServiceState.STARTING, ServiceState.STARTED);
		
		client.run();
		
		clientStates.checkNow();
		
		client.destroy();
		
		((Stoppable) server).stop();
	}
}
