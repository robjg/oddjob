package org.oddjob.values.properties;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.oddjob.arooa.runtime.PropertyLookup;
import org.oddjob.arooa.runtime.PropertySource;

/**
 * A {@link PropertyLookup} for environment variables.
 * 
 * @author rob
 *
 */
public class EnvVarPropertyLookup implements PropertyLookup {

	final PropertySource SOURCE = new PropertySource() {
		public String toString() {
			return "ENVIRONMENT";
		}
	};
	
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

	@Override
	public Set<String> propertyNames() {
		
		Map<String, String> all = System.getenv();
		Set<String> names = new TreeSet<String>();
		for (String key : all.keySet()) {
			names.add(prefixPlusDot + key);
		}
		return names;
	}
	
	@Override
	public PropertySource sourceFor(String propertyName) {
		if (lookup(propertyName) != null) {
			return SOURCE;
		}
		else {
			return null;
		}
	}	
}
