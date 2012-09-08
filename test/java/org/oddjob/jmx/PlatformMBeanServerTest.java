package org.oddjob.jmx;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConsoleCapture;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobComponentResolver;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.EchoJob;
import org.oddjob.state.ParentState;

public class PlatformMBeanServerTest extends TestCase {

	private static final Logger logger = Logger.getLogger(PlatformMBeanServerTest.class);

	@Override
	protected void setUp() throws Exception {
		logger.info("----------------------  " + getName() + "  --------------------");
	}
	
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
	
	public void testInOddjob() {
		
		Oddjob server = new Oddjob();
		server.setConfiguration(new XMLConfiguration(
				"org/oddjob/jmx/PlatformMBeanServerExample.xml", 
				getClass().getClassLoader()));
		
		server.run();
		
		Oddjob client = new Oddjob();
		client.setConfiguration(new XMLConfiguration(
				"org/oddjob/jmx/PlatformMBeanClientExample.xml", 
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		client.run();
		
		console.close();
		
		assertEquals(ParentState.COMPLETE, 
				client.lastStateEvent().getState());
		
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals("Hello from an Oddjob Server!", lines[0].trim());
		assertEquals(1, lines.length);
		
		client.destroy();
		
		server.destroy();
	}
	
}
