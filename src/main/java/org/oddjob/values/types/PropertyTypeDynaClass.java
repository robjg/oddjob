package org.oddjob.values.types;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.MutableDynaClass;

/**
 * <p>A DynaClass for the PropertyType which always returns
 * {@link org.oddjob.values.types.PropertyType} as the type
 * for all properties.
 * 
 * @see org.apche.commons.beanutils.LazyDynaBean
 * @author Rob Gordon
 */
public class PropertyTypeDynaClass implements MutableDynaClass, Serializable {
	private static final long serialVersionUID = 20070124;

	/** Map of property name to DynaProperty. */
	private final Map<String, DynaProperty> propertiesMap = 
		new HashMap<String, DynaProperty>();
	
	/** The name for getName() */
	private final String name; 
	
	/**
	 * Constructor.
	 * 
	 * @param name The name.
	 */
	public PropertyTypeDynaClass(String name) {
		this.name = name;
	}
	
	/**
	 * This MutableDynaClass is never restricted.
	 * 
	 * @see org.apache.commons.beanutils.MutableDynaClass#isRestricted()
	 */
	@Override
    public boolean isRestricted() {
        return false;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.commons.beanutils.MutableDynaClass#setRestricted(boolean)
     */
	@Override
    public void setRestricted(boolean restricted) {
        throw new UnsupportedOperationException("Can not restrict this MutableDynaClass.");
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.commons.beanutils.MutableDynaClass#add(java.lang.String)
     */
	@Override
    public void add(String name) {
        add(new DynaProperty(name, String.class));
    }

    /**
     * Add a new dynamic property with the specified data type, but with
     * no restrictions on readability or writeability.
     *
     * @param name Name of the new dynamic property
     * @param type Data type of the new dynamic property (null for no
     *  restrictions)
     *
     * @exception IllegalArgumentException if name is null
     * @exception IllegalStateException if this DynaClass is currently
     *  restricted, so no new properties can be added
     */
	@Override
    public void add(String name, 
    		@SuppressWarnings("rawtypes") Class type) {
    	if (! (String.class.isAssignableFrom(type))) {
    		throw new IllegalArgumentException("Only PropertyType properties are allowed.");
    	}
        add(new DynaProperty(name, type));
    }

    /**
     * <p>Add a new dynamic property with the specified data type, readability,
     * and writeability.</p>
     *
     * <p><strong>N.B.</strong>Support for readable/writeable properties has not been implemented
     *    and this method always throws a <code>UnsupportedOperationException</code>.</p>
     *
     * <p>I'm not sure the intention of the original authors for this method, but it seems to
     *    me that readable/writable should be attributes of the <code>DynaProperty</code> class
     *    (which they are not) and is the reason this method has not been implemented.</p>
     *
     * @param name Name of the new dynamic property
     * @param type Data type of the new dynamic property (null for no
     *  restrictions)
     * @param readable Set to <code>true</code> if this property value
     *  should be readable
     * @param writeable Set to <code>true</code> if this property value
     *  should be writeable
     *
     * @exception UnsupportedOperationException anytime this method is called
     */
	@Override
    public void add(String name, @SuppressWarnings("rawtypes") Class type, 
    		boolean readable, boolean writeable) {
        throw new java.lang.UnsupportedOperationException(
        		"readable/writable properties not supported");
    }

    /**
     * Add a new dynamic property.
     *
     * @param property Property the new dynamic property to add.
     *
     * @exception IllegalArgumentException if name is null
     * @exception IllegalStateException if this DynaClass is currently
     *  restricted, so no new properties can be added
     */
    protected void add(DynaProperty property) {

        if (property.getName() == null) {
            throw new IllegalArgumentException("Property name is missing.");
        }

        // Check if property already exists
        if (propertiesMap.get(property.getName()) != null) {
           return;
        }

        propertiesMap.put(property.getName(), property);

    }

    /**
     * Remove the specified dynamic property, and any associated data type,
     * readability, and writeability, from this dynamic class.
     * <strong>NOTE</strong> - This does <strong>NOT</strong> cause any
     * corresponding property values to be removed from DynaBean instances
     * associated with this DynaClass.
     *
     * @param name Name of the dynamic property to remove
     *
     * @exception IllegalArgumentException if name is null
     * @exception IllegalStateException if this DynaClass is currently
     *  restricted, so no properties can be removed
     */
	@Override
    public void remove(String name) {

        if (name == null) {
            throw new IllegalArgumentException("Property name is missing.");
        }

        // Ignore if property doesn't exist
        if (propertiesMap.get(name) == null) {
            return;
        }

        propertiesMap.remove(name);
        // Create a new property array of without the specified property

    }

    /**
     * <p>Return a property descriptor for the specified property.</p>
     *
     * <p>If the property is not found and the <code>returnNull</code> indicator is
     *    <code>true</code>, this method always returns <code>null</code>.</p>
     *
     * <p>If the property is not found and the <code>returnNull</code> indicator is
     *    <code>false</code> a new property descriptor is created and returned (although
     *    its not actually added to the DynaClass's properties). This is the default
     *    beahviour.</p>
     *
     * <p>The reason for not returning a <code>null</code> property descriptor is that
     *    <code>BeanUtils</code> uses this method to check if a property exists
     *    before trying to set it - since these <i>Lazy</i> implementations automatically
     *    add any new properties when they are set, returning <code>null</code> from
     *    this method would defeat their purpose.</p>
     *
     * @param name Name of the dynamic property for which a descriptor
     *  is requested
     *
     * @exception IllegalArgumentException if no property name is specified
     */
	@Override
    public DynaProperty getDynaProperty(String name) {

        if (name == null) {
            throw new IllegalArgumentException("Property name is missing.");
        }

        DynaProperty dynaProperty = propertiesMap.get(name);

        // If it doesn't exist create a new one.
        if (dynaProperty == null) {
            dynaProperty = new DynaProperty(name, String.class);
        }

        return dynaProperty;

    }

    /*
     *  (non-Javadoc)
     * @see org.apache.commons.beanutils.DynaClass#getDynaProperties()
     */
	@Override
    public DynaProperty[] getDynaProperties() {
    	return (DynaProperty[]) propertiesMap.values().toArray(new DynaProperty[0]);
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.commons.beanutils.DynaClass#getName()
     */
	@Override
    public String getName() {
    	return name;
    }    
    
    /*
     *  (non-Javadoc)
     * @see org.apache.commons.beanutils.DynaClass#newInstance()
     */
	@Override
    public DynaBean newInstance() throws IllegalAccessException, InstantiationException {
    	throw new UnsupportedOperationException("newInsance() is unsupported!");
    }    

}