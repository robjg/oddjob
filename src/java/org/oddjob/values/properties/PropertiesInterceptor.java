package org.oddjob.values.properties;

import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.ParsingInterceptor;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.SessionOverrideContext;

/**
 * Provide a new property manager so that a component that uses this
 * and all it's children can have their own property context.
 * 
 * @author rob
 *
 */
public class PropertiesInterceptor implements ParsingInterceptor {

	@Override
	public ArooaContext intercept(ArooaContext suggestedContext)
			throws ArooaConfigurationException {
		
		return new SessionOverrideContext(suggestedContext, 
				new PropertiesConfigurationSession(
						suggestedContext.getSession()));
	}
}
