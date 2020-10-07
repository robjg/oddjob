package org.oddjob.events;

import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.state.StateOperator;

import java.util.HashMap;

/**
 * Provide {@link EventOperator}s for Collection {@link EventSource}.
 */
public class EventOperators {

	public static final String ALL = "ALL";
	public static final String ANY = "ANY";

	private static final HashMap<String, EventOperator<?>> eventOperators =
			new HashMap<>();

	static {
		
		eventOperators.put(ALL, new AllEvents<>());
		eventOperators.put(ANY, new AnyEvents<>());
	}
	
	/**
	 * The Conversion from String. Required so the default converter
	 * can then make the association to a {@link StateOperator}.
	 */
	public static class Conversions implements ConversionProvider {
		@Override
		public void registerWith(ConversionRegistry registry) {
			registry.register(String.class, EventOperator.class,
					from -> {
						EventOperator<?> eventOperator = eventOperators.get(
								from.toUpperCase());
						
						if (eventOperator == null) {
							throw new ConvertletException(
									"Valid values are " + 
							eventOperators.keySet());
						}
						
						return eventOperator;
					});
		}
	}	

}
