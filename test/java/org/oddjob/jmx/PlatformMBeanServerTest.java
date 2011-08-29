package org.oddjob.jmx;

import junit.framework.TestCase;

import org.oddjob.Helper;
import org.oddjob.OddjobComponentResolver;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jobs.EchoJob;

public class PlatformMBeanServerTest extends TestCase {

	public void testClientServer() throws Exception {
		
		Object echo = new OddjobComponentResolver().resolve(new EchoJob(), null);

		JMXServerJob server = new JMXServerJob();
		
		server.setRoot(echo);
		server.setArooaSession(new StandardArooaSession());
		server.start();
		
		JMXClientJob client = new JMXClientJob();
		client.setArooaSession(new StandardArooaSession());
		client.run();
		
		Object[] children = Helper.getChildren(client);
		
		assertEquals(1, children.length);
		
		client.stop();
		
		server.stop();
	}
	
}
