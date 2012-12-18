package org.oddjob.state;

import java.util.Arrays;
import java.util.HashMap;

import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;

public class StateOperators {

	public static final String WORST = "WORST";
	public static final String ACTIVE = "ACTIVE";
	public static final String SERVICES = "SERVICES";
	
	private static final HashMap<String, StateOperator> stateOperators =
			new HashMap<String, StateOperator>();

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
					new Convertlet<String, StateOperator>() {
				@Override
				public StateOperator convert(String from)
				throws ConvertletException {
					StateOperator stateOperator = stateOperators.get(
							from.toUpperCase());
					
					if (stateOperator == null) {
						throw new ConvertletException(
								"Valid values are " + 
						stateOperators.keySet());
					}
					
					return stateOperator;
				}
			});
		}
	}	

}
