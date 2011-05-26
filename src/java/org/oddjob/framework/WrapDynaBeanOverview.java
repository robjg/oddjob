package org.oddjob.framework;

import org.apache.commons.beanutils.DynaProperty;
import org.oddjob.arooa.reflect.ArooaNoPropertyException;
import org.oddjob.arooa.reflect.BeanOverview;

public class WrapDynaBeanOverview implements BeanOverview {

	private WrapDynaClass dynaClass;
		
	public WrapDynaBeanOverview(WrapDynaClass dynaClass) {
		this.dynaClass = dynaClass;
	}
	
	public String[] getProperties() {
		DynaProperty[] properties = dynaClass.getDynaProperties();
		String[] names = new String[properties.length];
		
		for (int i = 0; i < properties.length; ++i) {
			names[i] = properties[i].getName();
		}
		
		return names;
	}
	
	public Class<?> getPropertyType(String property)
			throws ArooaNoPropertyException {
		DynaProperty dynaProperty = dynaClass.getDynaProperty(property);
		if (dynaProperty == null) {
			throw new ArooaNoPropertyException(property, dynaClass.getClass());
		}
		
		Class<?> propertyType;
		if (dynaProperty.isIndexed() || dynaProperty.isMapped()) {
			propertyType = dynaProperty.getContentType();
		}
		else {
			propertyType = dynaProperty.getType();
		}
		
		if (propertyType == null) {
			return null;
		}
		
		return propertyType;		
	}
	
	public boolean hasReadableProperty(String property) {
		if (dynaClass.getDynaProperty(property) == null) {
			return false;
		}
		return dynaClass.isReadable(property);
	}
	
	public boolean hasWriteableProperty(String property) {
		if (dynaClass.getDynaProperty(property) == null) {
			return false;
		}
		return dynaClass.isWritable(property);
	}
	
	public boolean isIndexed(String property) throws ArooaNoPropertyException {
		DynaProperty dynaProperty = dynaClass.getDynaProperty(property);
		if (dynaProperty == null) {
			throw new ArooaNoPropertyException(property, dynaClass.getClass());
		}
		return dynaProperty.isIndexed();
	}
	
	public boolean isMapped(String property) throws ArooaNoPropertyException {
		DynaProperty dynaProperty = dynaClass.getDynaProperty(property);
		if (dynaProperty == null) {
			throw new ArooaNoPropertyException(property, dynaClass.getClass());
		}
		return dynaProperty.isMapped();
	}
	
}
