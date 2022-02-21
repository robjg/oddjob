package org.oddjob.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * Used to alter behaviour between having a remote connector
 * and using the Platform MBean Server.
 * 
 * @author rob
 *
 */
abstract public class ServerStrategy {
	
	public static ServerStrategy stratagyFor(String url) throws MalformedURLException {
		if (url == null) {
			return new PlatformMBeanServerStrategy();
		}
		else {
			return new ConnectorServerStrategy(url);
		}
	}

	public static ServerStrategy strategyForPlatform() {
		return new PlatformMBeanServerStrategy();
	}

	public abstract MBeanServer findServer();
	
	public abstract String serverIdText() throws JMException;
	
	public abstract JMXConnectorServer startConnector(Map<String, ?> environment)
	throws IOException;
	
	public abstract String getAddress();
	
}

class ConnectorServerStrategy extends ServerStrategy {
	private static final Logger logger = LoggerFactory.getLogger(ConnectorServerStrategy.class);	
	
	private final JMXServiceURL serviceURL;
	
	private MBeanServer server;
	
	private String address;
	
	public ConnectorServerStrategy(String url) throws MalformedURLException {
		serviceURL = new JMXServiceURL(url);
	}
	
	@Override
	public MBeanServer findServer() {
		server = MBeanServerFactory.createMBeanServer();
		return server;
	}
	
	@Override
	public String serverIdText() {
		return serviceURL.getURLPath();
	}
	
	@Override
	public JMXConnectorServer startConnector(Map<String, ?> environment) throws IOException {
		JMXConnectorServer cntorServer = JMXConnectorServerFactory.newJMXConnectorServer(
				serviceURL, environment, server);
		
		cntorServer.start();
		address = cntorServer.getAddress().toString();
		logger.info("Server started. Clients may connect to: " + address);
		
		return cntorServer;
	}
	
	@Override
	public String getAddress() {
		return address;
	}
}

class PlatformMBeanServerStrategy extends ServerStrategy {
	private static final Logger logger = LoggerFactory.getLogger(PlatformMBeanServerStrategy.class);	
	
	@Override
	public MBeanServer findServer() {
		return ManagementFactory.getPlatformMBeanServer();
	}
	
	@Override
	public String serverIdText() throws JMException {
		return (String) findServer().getAttribute(
				new ObjectName("JMImplementation:type=MBeanServerDelegate"), "MBeanServerId");
	}
	
	@Override
	public JMXConnectorServer startConnector(Map<String, ?> environment) {
		logger.info("Server started using the Platform MBean Server.");
		return null;
	}	
	
	@Override
	public String getAddress() {
		return null;
	}
}
