package org.oddjob.jmx.server;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.xml.XMLConfiguration;

/**
 * An {@link HandlerFactoryProvider} that loads {@link HandlerFactoryProvider}s
 * from XML configuration found at the given URLs.
 * 
 * @author rob
 *
 */
public class URLFactoryProvider implements HandlerFactoryProvider {

	/** The URLs. */
	private final URL[] urls;
	
	/** The session for the parser to use. */
	private final ArooaSession session;

	/**
	 * Constructor.
	 * 
	 * @param urls
	 * @param session
	 */
	public URLFactoryProvider(URL[] urls, ArooaSession session) {
		this.urls = urls;
		this.session = session;
	}
	
	public ServerInterfaceHandlerFactory<?, ?>[] getHandlerFactories() {
		
		if (urls.length == 0) {
			return null;
		}

		Map<ServerInterfaceHandlerFactory<?, ?>, URL> allFactories = new HashMap<>();
		
		try {
				
			for (URL url: urls ) {
				
				XMLConfiguration config = new XMLConfiguration(
						url.toString(), url.openStream());
				
				StandardFragmentParser parser = 
					new StandardFragmentParser(session);
				
				parser.parse(config);
				
				HandlerFactoryProvider provider = 
					(HandlerFactoryProvider) parser.getRoot();
				
				ServerInterfaceHandlerFactory<?, ?>[] theseFactories = provider.getHandlerFactories();

				for (ServerInterfaceHandlerFactory<?, ?> factory : theseFactories) {

					if (allFactories.containsKey(factory)) {
						throw new IllegalArgumentException("Can't add Service Interface Handler Factory [" + 
							factory + "] from URL [" + url + "] as one already exists from URL [" + 
								allFactories.get(factory) + "]");
					}

					allFactories.put(factory, url);
				}
				
			}
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return allFactories.keySet().toArray(new ServerInterfaceHandlerFactory<?, ?>[allFactories.size()]);
	}
	
}
