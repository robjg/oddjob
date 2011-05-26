package org.oddjob.values.properties;

import org.oddjob.arooa.runtime.PropertyLookup;

public class EnvVarPropertyLookup implements PropertyLookup {

	private final String prefixPlusDot;
	
	public EnvVarPropertyLookup(String prefix) {
		this.prefixPlusDot = prefix + ".";
	}
	
	@Override
	public String lookup(String propertyName) {
		
		if (!propertyName.startsWith(prefixPlusDot)) {
			return null;
		}

		String envVar = propertyName.substring(prefixPlusDot.length());
		
		return System.getenv(envVar);
	}
	
}
