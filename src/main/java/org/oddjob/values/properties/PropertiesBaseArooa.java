package org.oddjob.values.properties;

import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ConfiguredHow;
import org.oddjob.arooa.ParsingInterceptor;
import org.oddjob.arooa.deploy.NoAnnotations;
import org.oddjob.arooa.utils.PropertiesOverrideSession;

/**
 * Base class for the Arooa descriptor classes for {@link PropertiesJob}
 * and {@link PropertiesType}. These provide a {@link ParsingInterceptor}
 * that replaces the {@link ArooaSession} for the parsing of child elements
 * with a {@link PropertiesOverrideSession}.
 * 
 * @see PropertiesJobArooa
 * @see PropertiesTypeArooa
 * 
 * @author rob
 *
 */
public class PropertiesBaseArooa implements ArooaBeanDescriptor {

	@Override
	public ParsingInterceptor getParsingInterceptor() {
		return new PropertiesInterceptor();
	}
	
	@Override
	public String getComponentProperty() {
		return null;
	}
	
	@Override
	public ConfiguredHow getConfiguredHow(String property) {
		if ("arooaContext".equals(property)) {
			return ConfiguredHow.HIDDEN;
		}
		if ("arooaSession".equals(property)) {
			return ConfiguredHow.HIDDEN;
		}
		return null;
	}
	
	@Override
	public String getFlavour(String property) {
		return null;
	}
	
	@Override
	public String getTextProperty() {
		return null;
	}
	
	@Override
	public boolean isAuto(String property) {
		return false;
	}
	
	@Override
	public ArooaAnnotations getAnnotations() {
		return new NoAnnotations();
	}
}
