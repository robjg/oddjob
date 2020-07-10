package org.oddjob.jmx;

import org.oddjob.OddjobException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.describe.NoDescribe;
import org.oddjob.jmx.server.*;
import org.oddjob.util.SimpleThreadManager;
import org.oddjob.util.ThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnectorServer;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * @oddjob.description A service which allows a job hierarchy to
 * be monitored and managed remotely using a {@link JMXClientJob}. 
 * <p>
 * Security can be added using the environment property. Simple JMX security comes
 * prepackaged as {@link SimpleServerSecurity}. Note that the access file is
 * an Oddjob specific access file. Oddjob requires full read/write access because
 * it uses JMX operations and all JMX operation require full read/write access.
 * Oddjob uses a JMX access format file but provides it's own primitive access 
 * control on top the JMX layer. Oddjob's access control removes an entire java 
 * interface from the client side proxy if any of it's methods are write.
 * One affect of this is that a read only account can't access properties of
 * the remote job with the ${server/remote-job} syntax because this functionality
 * is provided by the same interface (BeanUtils <code>DynaBean</code>) that allows
 * a remote job's properties to be written.
 * <p> 
 * For more information on JMX Security see
 * <a href="http://java.sun.com/javase/6/docs/technotes/guides/jmx/tutorial/security.html">
 * The JMX Tutorial</a>.
 * <p>
 * This service will use the Platform MBeanServer if no <code>url</code> 
 * property is provided. Creating an unsecured Oddjob server on a private
 * network can be achieved simply by launching Oddjob with a command line
 * such as:</p>
 * <pre>
 * java -Dcom.sun.management.jmxremote.port=nnnn \
 * -Dcom.sun.management.jmxremote.ssl=false \
 * -Dcom.sun.management.jmxremote.authenticate=false \
 * -jar run-oddjob.jar -f my-config.xml
 * </pre>
 * And then including this service somewhere in the configuration. Note that
 * the properties must be to the left of the -jar, not to the right because
 * the must be available to the JVM before Oddjob starts.
 * <p>
 * The <code>server.xml</code> Oddjob configration file in Oddjob's top
 * level directory provides a simple Oddjob server that uses an RMI 
 * Registry.
 * <p>
 * More information on Oddjob servers can be found in the User Guide under
 * 'Sharing Jobs on the Network'.
 * <p>
 * 
 * @oddjob.example
 * 
 * Creating a server using the platform MBean Server.
 * 
 * {@oddjob.xml.resource org/oddjob/jmx/PlatformMBeanServerExample.xml}
 * 
 * This is probably the simplest way to launch Oddjob as a server.
 * <p>
 * Here's an example of the command used to launch it:
 * <pre>
 * java -Dcom.sun.management.jmxremote.port=13013 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -jar C:\Users\rob\projects\oddjob\run-oddjob.jar -f C:\Users\rob\projects\oddjob\test\java\org\oddjob\jmx\PlatformMBeanServerExample.xml
 * </pre>
 * For an example of a client to connect to this server see the first 
 * example for {@link JMXClientJob}.
 * 
 * @oddjob.example
 * 
 * Creating a server using an RMI registry.
 * 
 * {@oddjob.xml.resource org/oddjob/jmx/ServerExample.xml}
 * 
 * The nested Oddjob can be any normal Oddjob configuration. Here is the 
 * nested Oddjob used in some client examples. The greeting is in 
 * a folder because it will only be run from the client.
 * 
 * {@oddjob.xml.resource org/oddjob/jmx/ServerJobs.xml}
 * 
 * @oddjob.example
 * 
 * Creating a secure server.
 * 
 * {@oddjob.xml.resource org/oddjob/jmx/SecureServerExample.xml}
 * 
 * @author Rob Gordon
 */
public class JMXServerJob implements ArooaSessionAware {
	private static final Logger logger = LoggerFactory.getLogger(JMXServerJob.class);
	
	public static final String ACCESS_FILE_PROPERTY = "oddjob.jmx.remote.x.access.file";
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private String name;

	/** 
	 * @oddjob.property 
	 * @oddjob.description The root node.
	 * @oddjob.required Yes.
	 */
	private Object root;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The JMX service URL. If this is not provided the 
	 * server connects to the Platform MBean Server.
	 * @oddjob.required No. 
	 */
	private String url;

	/** 
	 * @oddjob.property
	 * @oddjob.description The log format for formatting log messages. For more
	 * information on the format please see <a href="http://logging.apache.org/log4j/docs/">
	 * http://logging.apache.org/log4j/docs/</a>
	 * @oddjob.required No.
	 */
	private String logFormat;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Additional handler factories that allow
	 * any interface to be invoked from a remote Oddjob.
	 * 
	 * @oddjob.required No.
	 */
	private HandlerFactoryProvider handlerFactories;
	
	/**
	 * The ThreadManager. Handlers use this to avoid long running
	 * connections.
	 */	
	private ThreadManager threadManager;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The address of this server. This is mainly 
	 * useful for testing 
	 */
	private String address;

	/** Remember the registry so it can be used to resolve ids for the
	 * client.
	 */
	private ArooaSession session;
	
	/** Bean Factory */
	private OddjobMBeanFactory factory;
	
	/** Connector server */
	private JMXConnectorServer cntorServer; 

	/** 
	 * @oddjob.property
	 * @oddjob.description An environment such
	 * as security settings. 
	 * 
	 * @oddjob.required No.
	 */
	private Map<String, ?> environment;

	@ArooaHidden
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name
	 * 
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Set the root node directly.
	 * 
	 * @param rootNode The root node for the monitor tree.
	 */
	@ArooaAttribute
	public void setRoot(Object rootNode) {
		this.root = rootNode;
	}

	/**
	 * Get the root node of the monitor tree.
	 * 
	 * @return The root node.
	 */
	public Object getRoot() {
		return this.root;
	}

	public String getAddress() {
		return address;
	}
	
	/**
	 * Set the name to bind the root node as in the naming service.
	 * 
	 * @param url The name for the naming service.
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * Get the name the root node is bound as in the naming service.
	 * 
	 * @return The name used in the naming service.
	 */
	public String getUrl() {
		return url;
	}

	@NoDescribe
	public MBeanServerConnection getServerConnection() {
		return Optional.ofNullable(this.cntorServer)
				.map(cs -> cs.getMBeanServer())
				.orElse(null);
	}

	public void start() 
	throws JMException, IOException, ServerLoopBackException {
		if (root == null) {			
			throw new OddjobException("No root node.");
		}
		
		ServerStrategy serverStrategy = ServerStrategy.stratagyFor(url);
		
		MBeanServer server = serverStrategy.findServer(); 

		threadManager = new SimpleThreadManager();
		
		// Add supported interfaces.
		// note that some interfaces are hardwired in the factory because
		// they are aspects of the server.
		ServerInterfaceManagerFactoryImpl imf = 
			new ServerInterfaceManagerFactoryImpl(environment);
		
		ServerInterfaceHandlerFactory<?, ?>[] sihfs = new ResourceFactoryProvider(session
				).getHandlerFactories();
		
		imf.addServerHandlerFactories(sihfs);

		if (handlerFactories != null) {
			imf.addServerHandlerFactories(handlerFactories.getHandlerFactories());
		}
		
		BeanDirectory registry = session.getBeanRegistry();
				
		ServerModelImpl model = new ServerModelImpl(
				new ServerId(serverStrategy.serverIdText()),
				threadManager, 
				imf);
		
		model.setLogFormat(logFormat);
			
		factory = new OddjobMBeanFactory(server, session);
		
		ServerMainBean serverBean = new ServerMainBean(
				root,
				registry);
		
		long mainName = factory.createMBeanFor(serverBean,
				new ServerContextMain(model, registry));
		if (mainName != 0L) {
			throw new IllegalStateException("Main bean id should be 0 not " + mainName);
		}

			
		this.cntorServer = serverStrategy.startConnector(environment);
		this.address = serverStrategy.getAddress();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception {
		logger.debug("Stopping any running jobs.");
		threadManager.close();
		
		logger.debug("Desroying MBeans.");
		try {
			factory.destroy(0L);
		}
		catch (JMException e) {
			// This can happen when the RMI registry is shut before the 
			// server is stopped.
			logger.error("Failed destroying main MBean.", e);
		}
		
		if (cntorServer != null) {
			logger.debug("Stopping JMXConnectorServer.");
			this.address = null;
			cntorServer.stop();
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (name == null) {
			return "Oddjob Server";
		}
		return name;
	}

	public String getLogFormat() {
		return logFormat;
	}

	public void setLogFormat(String logFormat) {
		this.logFormat = logFormat;
	}

	public HandlerFactoryProvider getHandlerFactories() {
		return handlerFactories;
	}

	public void setHandlerFactories(HandlerFactoryProvider handlerFactories) {
		this.handlerFactories = handlerFactories;
	}
	
	public Map<String, ?> getEnvironment() {
		return environment;
	}

	public void setEnvironment(Map<String, ?> environment) {
		this.environment = environment;
	}
}
