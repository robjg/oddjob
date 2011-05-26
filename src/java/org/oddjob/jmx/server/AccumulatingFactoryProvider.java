package org.oddjob.jmx.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An {@link HandlerFactoryProvider} that provides {@ServerInterfaceHandler}s 
 * by accumulating other <code>ServerHandlerProvider<code>s.
 * 
 * @author rob
 */
public class AccumulatingFactoryProvider implements HandlerFactoryProvider {

	/** The providers. */
	private List<HandlerFactoryProvider> providers = 
		new ArrayList<HandlerFactoryProvider>();	
	
	/**
	 * Add a provider.
	 * 
	 * @param provider
	 */
	public void addProvider(HandlerFactoryProvider provider) {
		providers.add(provider);
	}
	
	public ServerInterfaceHandlerFactory<?, ?>[] getHandlerFactories() {
		List<ServerInterfaceHandlerFactory<?, ?>> results = 
			new ArrayList<ServerInterfaceHandlerFactory<?, ?>>();
		
		for (HandlerFactoryProvider provider : providers ) {
			results.addAll(Arrays.asList(provider.getHandlerFactories()));
		}
		
		return results.toArray(new ServerInterfaceHandlerFactory<?, ?>[results.size()]);
	}	
}
