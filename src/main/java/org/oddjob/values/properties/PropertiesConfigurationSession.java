package org.oddjob.values.properties;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.parsing.SessionDelegate;
import org.oddjob.arooa.runtime.PropertyManager;
import org.oddjob.arooa.standard.StandardPropertyManager;
import org.oddjob.values.types.PropertyType;

/**
 * A version of an {@link ArooaSession} that creates it's own copy
 * of a {@link PropertyManager} so that it can resolve properties during
 * configuration of the {@link PropertyType} and {@link PropertyJob}.
 * 
 * @author rob
 *
 */
public class PropertiesConfigurationSession extends SessionDelegate
implements ArooaSession {

	private final PropertyManager propertyManager;
	
	/**
	 * Constructor.
	 * 
	 * @param original The original session.
	 */
	public PropertiesConfigurationSession(ArooaSession original) {
		super(original);
		
		this.propertyManager = new StandardPropertyManager(
				original.getPropertyManager());
	}
		
	@Override
	public PropertyManager getPropertyManager() {
		return propertyManager;
	}		
		
}
