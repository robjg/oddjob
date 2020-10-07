package org.oddjob.state;

import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.ConvertletException;

import java.util.HashMap;

/**
 * Provide {@link StateOperator}s for structural jobs
 */
public class StateOperators {

	public static final String WORST = "WORST";
	public static final String ACTIVE = "ACTIVE";
	public static final String SERVICES = "SERVICES";
	
	private static final HashMap<String, StateOperator> stateOperators =
			new HashMap<>();

	static {
		
		stateOperators.put(WORST, new WorstStateOp());
		stateOperators.put(ACTIVE, new AnyActiveStateOp());
		stateOperators.put(SERVICES, new ServiceManagerStateOp());
	}
	
	/**
	 * The Conversion from String. Required so the default converter
	 * can then make the association to a {@link StateOperator}.
	 */
	public static class Conversions implements ConversionProvider {
		@Override
		public void registerWith(ConversionRegistry registry) {
			registry.register(String.class, StateOperator.class,
					from -> {
						StateOperator stateOperator = stateOperators.get(
								from.toUpperCase());

						if (stateOperator == null) {
							throw new ConvertletException(
									"Valid values are " +
							stateOperators.keySet());
						}

						return stateOperator;
					});
		}
	}	

}
