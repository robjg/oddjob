package org.oddjob.jmx.client;

import org.oddjob.arooa.ClassResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Helps the client dealing with a lot of {@link ClientHandlerResolver}s.
 * 
 * @author rob
 *
 */
public class ResolverHelper {

	private final ClassResolver classResolver;
	
	public ResolverHelper(ClassResolver classResolver) {
		this.classResolver = classResolver;
	}
	
	public ClientInterfaceHandlerFactory<?>[] resolveAll(
			ClientHandlerResolver<?>[] resolvers) {
	
		List<ClientInterfaceHandlerFactory<?>> results =
				new ArrayList<>();
		
		for (ClientHandlerResolver<?> resolver: resolvers) {
			ClientInterfaceHandlerFactory<?> factory = 
				resolver.resolve(classResolver);

			if (factory != null) {
				results.add(factory);
			}
		}
		
		return results.toArray(
				new ClientInterfaceHandlerFactory<?>[0]);
	}
}
