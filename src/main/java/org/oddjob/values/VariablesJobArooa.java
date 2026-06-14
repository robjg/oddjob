package org.oddjob.values;

import org.oddjob.arooa.*;
import org.oddjob.arooa.deploy.NoAnnotations;

import java.beans.BeanDescriptor;
import java.lang.annotation.Annotation;

/**
 * Provide a {@link BeanDescriptor} for {@link VariablesJob}.
 * 
 * @author rob
 *
 */
public class VariablesJobArooa implements ArooaBeanDescriptor {

	@Override
	public String getComponentProperty() {
		return null;
	}

	@Override
	public ParsingInterceptor getParsingInterceptor() {
		return null;
	}

	@Override
	public String getTextProperty() {
		return null;
	}

	@Override
	public ConfiguredHow getConfiguredHow(String property) {
		if (ArooaConstants.ID_PROPERTY.equals(property)) {
			return ConfiguredHow.ATTRIBUTE;
		}
		return ConfiguredHow.ELEMENT;
	}

	@Override
	public boolean isAuto(String property) {
		return false;
	}

	@Override
	public String getFlavour(String property) {
		return null;
	}

	@Override
	public Annotation getQualifier(String property) {
		return null;
	}

	@Override
	public ArooaAnnotations getAnnotations() {
		return new NoAnnotations();
	}
}
