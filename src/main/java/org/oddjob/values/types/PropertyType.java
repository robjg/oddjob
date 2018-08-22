/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.values.types;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.MutableDynaClass;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.values.properties.PropertiesJob;

/**
 * @oddjob.description This is an internal type used to allow nested properties
 * in a {@link PropertiesJob}.
 */
public class PropertyType implements DynaBean, Serializable {
	private static final long serialVersionUID = 2009042200L;
	
	public static class Conversions implements ConversionProvider {
		public void registerWith(ConversionRegistry registry) {
			registry.register(PropertyType.class, String.class, 
					new Convertlet<PropertyType, String>() {
				public String convert(PropertyType from) throws ConvertletException {
					return from.value;
				}
			});
			
			registry.register(PropertyType.class, Properties.class, 
					new Convertlet<PropertyType, Properties>() {
				public Properties convert(PropertyType from) throws ConvertletException {
					return from.toProperties();
				}
			});
		}
	}
	
	/** Nested properties. */
	private final Map<String, PropertyType> props = 
		new HashMap<String, PropertyType>();

	/** The dynaClass. Should name be unique? - don't know. */
	private final MutableDynaClass dynaClass = new PropertyTypeDynaClass(PropertyTypeDynaClass.class.getName());
	
	/** The value of this property. */
	private String value;
	
	/** The name of this property. */
	private final String name;
	
	/**
	 * Only public constructor. Used to create the root of a property
	 * hierarchy.
	 *
	 */
	public PropertyType() {
		this.name = null;
	}
	
	/**
	 * Private contructor used by add and set.
	 * 
	 * @param name The name of the property being created.
	 */
	private PropertyType(String name) {
		this.name = name;
	}
	
	/**
	 * Accumulate properties.
	 * 
	 * @param props Properties accumulated so far.
	 */
	void properties(Properties result, String stem) {
		String propertyName = "";
		String nextStem = "";
		if (name != null) {
			propertyName = stem + name;
			nextStem = propertyName + ".";
		}
		
		if (value != null) {
			result.setProperty(propertyName, value);
		}
		for (String childName : props.keySet()) {
			PropertyType child = props.get(childName);
			child.properties(result, nextStem);
		}
	}

	/**
	 * Converts this PropertyType into Properties.
	 * 
	 * @return
	 */
	public Properties toProperties() {
		Properties props = new Properties();
		properties(props, "");
		return props;		
	}
	
	/**
	 * Get the size, which is the number of children,
	 * for this property.
	 * 
	 * @return The number of children.
	 */
	public int size() {
		return props.size();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.beanutils.DynaBean#contains(java.lang.String, java.lang.String)
	 */
	public boolean contains(String name, String key) {
		throw new UnsupportedOperationException("Can't perform key operations on properties.");
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.beanutils.DynaBean#get(java.lang.String)
	 */
	public Object get(String name) {
		PropertyType prop = props.get(name); 
		if (prop == null) {
			// no? - create a new PropertyType
			prop = new PropertyType(name);
			// and add it to the lazy bean.
			props.put(prop.name, prop);
			dynaClass.add(name);
		}
			
		return prop;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.beanutils.DynaBean#get(java.lang.String, int)
	 */
	public Object get(String name, int index) {
		throw new UnsupportedOperationException("Can't get indexed values on properties.");
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.beanutils.DynaBean#get(java.lang.String, java.lang.String)
	 */
	public Object get(String name, String key) {
		throw new UnsupportedOperationException("Can't get mapped values on properties.");
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.beanutils.DynaBean#getDynaClass()
	 */
	public DynaClass getDynaClass() {
		return dynaClass;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.apache.commons.beanutils.DynaBean#remove(java.lang.String, java.lang.String)
	 */
	public void remove(String name, String key) {
		throw new UnsupportedOperationException("Can't remove mapped values on properties.");
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.apache.commons.beanutils.DynaBean#set(java.lang.String, int, java.lang.Object)
	 */
	public void set(String name, int index, Object value) {
		throw new UnsupportedOperationException("Can't set mapped values on properties.");
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.beanutils.DynaBean#set(java.lang.String, java.lang.Object)
	 */
	public void set(String name, Object value) {

		// does our map already contain the value
		PropertyType prop = (PropertyType) props.get(name);
		if (prop == null) {
			// no? - create a new PropertyType
			prop = new PropertyType(name);
			// and add it to the map.
			props.put(prop.name, prop);
			dynaClass.add(name);
		}
		prop.value = (String) value;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.beanutils.DynaBean#set(java.lang.String, java.lang.String, java.lang.Object)
	 */
	public void set(String name, String key, Object value) {
		throw new UnsupportedOperationException("Can't set mapped values on properties.");
	}

	/**
	 * Override toString().
	 * 
	 * @return a String or null if the resolved value is null.
	 */
	public String toString() {
		if (value != null) {
			return "Property: " + name + "=" + value;
		}
		else if (name != null){
			return "Properties starting with " + name + ": " + size() + " sub types.";
		}
		else {
			return "Properties root: " + size() + " sub types.";
		}
	}
}
