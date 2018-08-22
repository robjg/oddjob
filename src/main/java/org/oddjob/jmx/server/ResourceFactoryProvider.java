package org.oddjob.jmx.server;

import java.net.URL;

import org.oddjob.arooa.ArooaSession;

/**
 * An {@link HandlerFactoryProvider} that provides actions from 
 * any number of XML Configurations found on the class path.
 * 
 * @author rob
 *
 */
public class ResourceFactoryProvider implements HandlerFactoryProvider {

	/** The resource name. */
	public static final String ACTION_FILE = "META-INF/oj-jmx.xml";
	
	/** The session to use for finding resources and parsing
	 * the configurations. */
	private final ArooaSession session;
	
	/**
	 * Constructor.
	 * 
	 * @param session
	 */
	public ResourceFactoryProvider(ArooaSession session) {
		this.session = session;
	}
	
	public ServerInterfaceHandlerFactory<?, ?>[] getHandlerFactories() {
		
		URL[] urls = session.getArooaDescriptor().getClassResolver().getResources(
				ACTION_FILE);
		
		return new URLFactoryProvider(urls, session).getHandlerFactories();
	}

}
