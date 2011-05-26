package org.oddjob.jmx;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Helper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.client.UsernamePassword;
import org.oddjob.jmx.server.SimpleServerSecurity;

public class SimpleSecurityTest extends TestCase {

	private static final Logger logger = Logger.getLogger(SimpleSecurityTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.debug("------------------- " + getName() + " --------------");
	}
	
	boolean useSSL;
	
	public void testSimpleOKExample() throws Exception {
	
		String param = System.getProperty("oddjob.jmx.test.security.config");
		if (param == null) {
			return;
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
		client.setUrl(server.getAddress());
		client.setArooaSession(new StandardArooaSession());
		client.setEnvironment(credentials.toValue());		
		client.run();
		
		Object[] children = Helper.getChildren(client);
		
		assertEquals(1, children.length);
		assertEquals("test", children[0].toString()); 
		
		client.stop();
		
		server.stop();
	}
	
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
}
