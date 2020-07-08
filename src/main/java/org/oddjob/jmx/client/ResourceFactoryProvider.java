package org.oddjob.jmx.client;

import org.oddjob.arooa.ArooaSession;

import java.net.URL;
import java.util.Objects;

/**
 * An {@link HandlerFactoryProvider} that provides actions from
 * any number of XML Configurations found on the class path.
 * 
 * @author rob
 *
 */
public class ResourceFactoryProvider implements HandlerFactoryProvider {

	/** The resource name. */
	public static final String ACTION_FILE = "META-INF/oj-client.xml";
	
	/** The session to use for finding resources and parsing
	 * the configurations. */
	private final ArooaSession session;
	
	/**
	 * Constructor.
	 * 
	 * @param session
	 */
	public ResourceFactoryProvider(ArooaSession session) {
		this.session = Objects.requireNonNull(session);
	}
	
	public ClientInterfaceHandlerFactory<?>[] getHandlerFactories() {
		
		URL[] urls = session.getArooaDescriptor().getClassResolver().getResources(
				ACTION_FILE);
		
		return new URLFactoryProvider(urls, session).getHandlerFactories();
	}

}
