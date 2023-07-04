package org.oddjob.values;

import org.apache.commons.beanutils.*;
import org.oddjob.arooa.ArooaConstants;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;
import org.oddjob.framework.extend.SimpleJob;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * @oddjob.description This job provides a 'variables' 
 * like declaration within Oddjob. 
 * <p>
 * The variables job is like a bean where any property can be set
 * with any value.
 * <p>
 * Because names are properties, they can only be valid simple property
 * names. 'java.version' is not valid simple property because it is
 * interpreted as a value 'java' that has a property 'version'. To allow
 * these type of properties to be referenced in Oddjob use 
 * {@link org.oddjob.values.properties.PropertiesJob}.
 * 
 * @oddjob.example
 * 
 * A simple variable.
 * 
 * {@oddjob.xml.resource org/oddjob/values/VariablesExample.xml}
 * 
 * 
 * @author rob
 */
public class VariablesJob extends SimpleJob
		implements DynaBean {
	
	private final Map<String, Object> values = new LinkedHashMap<>();
	
	/**
	 * The LazyDynaBean to delegate to.
	 */
	private final LazyDynaBean dynaBean = new LazyDynaMap(values);
	
	/**
	 * The DynaClass - every type is an ArooaValue.
	 */
	private final MutableDynaClass dynaClass = new VariablesDynaClass(
			dynaBean);
	
	/**
	 * Add a name value pair. This method will be called during parsing.
	 * 
	 * @param name The name of the variable.
	 * @param value The runtime configurable for the value.
	 */
	public void setValue(String name, ArooaValue value) {
		logger().debug("Setting [" + name + "] = [" + value + "]");
		dynaBean.set(name, value);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	@Override
	protected int execute() throws Exception {
    	
		return 0;
	}

	@Override
	protected void onReset() {
		// Stop concurrent modification exception.
		List<String> keySet = new ArrayList<>(values.keySet());
		
		for (String name : keySet) {
			if (ArooaConstants.ID_PROPERTY.equals(name)) {
				continue;
			}
			values.remove(name);
			dynaClass.remove(name);
		}
	}
	
    /**
     * Does the specified mapped property contain a value for the specified
     * key value?
     *
     * @param name Name of the property to check
     * @param key Name of the key to check
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     */
	@Override
    public boolean contains(String name, String key) {
    	return dynaBean.contains(name, key);
    }

    /**
     * Return the value of a simple property with the specified name.
     *
     * @param name Name of the property whose value is to be retrieved
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     */
	@Override
    public Object get(String name) {
        return dynaBean.get(name);
    }

    /**
     * Return the value of an indexed property with the specified name.
     *
     * @param name Name of the property whose value is to be retrieved
     * @param index Index of the value to be retrieved
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception IllegalArgumentException if the specified property
     *  exists, but is not indexed
     * @exception IndexOutOfBoundsException if the specified index
     *  is outside the range of the underlying property
     * @exception NullPointerException if no array or List has been
     *  initialized for this property
     */
	@Override
    public Object get(String name, int index) {
        return dynaBean.get(name, index);
    }


    /**
     * Return the value of a mapped property with the specified name,
     * or <code>null</code> if there is no value for the specified key.
     *
     * @param name Name of the property whose value is to be retrieved
     * @param key Key of the value to be retrieved
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception IllegalArgumentException if the specified property
     *  exists, but is not mapped
     */
	@Override
    public Object get(String name, String key) {
        return dynaBean.get(name, key);
    }

    /**
     * Return the <code>DynaClass</code> instance that describes the set of
     * properties available for this DynaBean.
     */
	@Override
    public DynaClass getDynaClass() {
        return dynaClass;
    }


    /**
     * Remove any existing value for the specified key on the
     * specified mapped property.
     *
     * @param name Name of the property for which a value is to
     *  be removed
     * @param key Key of the value to be removed
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     */
	@Override
    public void remove(String name, String key) {
    	dynaBean.remove(name, key);
    }
    
    /**
     * Set the value of a simple property with the specified name.
     *
     * @param name Name of the property whose value is to be set
     * @param value Value to which this property is to be set
     *
     * @exception ConversionException if the specified value cannot be
     *  converted to the type required for this property
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception NullPointerException if an attempt is made to set a
     *  primitive property to null
     */
	@Override
    public void set(String name, Object value) {
		logger().debug("Setting [" + name + "] = [" + value + "]");	
//		if (! (value instanceof ArooaValue)) {
//			throw new IllegalArgumentException("Variables can only be ArooaValue objects.");
//		}

		// will throw an exception if it fails.
		BeanUtilsPropertyAccessor.validateSimplePropertyName(name);
		
		dynaBean.set(name, value);
    }


    /**
     * Set the value of an indexed property with the specified name.
     *
     * @param name Name of the property whose value is to be set
     * @param index Index of the property to be set
     * @param value Value to which this property is to be set
     *
     * @exception ConversionException if the specified value cannot be
     *  converted to the type required for this property
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception IllegalArgumentException if the specified property
     *  exists, but is not indexed
     * @exception IndexOutOfBoundsException if the specified index
     *  is outside the range of the underlying property
     */
	@Override
    public void set(String name, int index, Object value) {
		logger().debug("Setting [" + name + "[" + index + "]] = [" + value + "]");    	
		dynaBean.set(name, index, value);
    }

    /**
     * Set the value of a mapped property with the specified name.
     *
     * @param name Name of the property whose value is to be set
     * @param key Key of the property to be set
     * @param value Value to which this property is to be set
     *
     * @exception ConversionException if the specified value cannot be
     *  converted to the type required for this property
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception IllegalArgumentException if the specified property
     *  exists, but is not mapped
     */
	@Override
    public void set(String name, String key, Object value) {
		logger().debug("Setting [" + name + "(" + key + ")] = [" + value + "]");
		
		dynaBean.set(name, key, value);
    }
        
    @Override
    public String toString() {
    	return "Variables: " + get(ArooaConstants.ID_PROPERTY);
    }
    
    /**
     * <p>A DynaClass for the variables which always returns
     * {@link org.oddjob.arooa.ArooaValue} as the type
     * for all properties.
     * 
     * @see org.apache.commons.beanutils.LazyDynaBean
     */
    static class VariablesDynaClass implements MutableDynaClass, Serializable {
    	private static final long serialVersionUID = 20070124;
    	
    	private final MutableDynaClass delegate;

    	VariablesDynaClass(DynaBean dynaBean) {
    		this.delegate = (MutableDynaClass) dynaBean.getDynaClass();
    	}
    	
    	/*
    	 *  (non-Javadoc)
    	 * @see org.apache.commons.beanutils.DynaClass#getDynaProperty(java.lang.String)
    	 */
    	@Override
        public DynaProperty getDynaProperty(String name) {
            DynaProperty dynaProperty = delegate.getDynaProperty(name);
            if (dynaProperty == null) {
            	throw new NullPointerException("LazyDynaClass should always return a DynaProperty!");
            }
            if (dynaProperty.getContentType() != null &&
            		ArooaValue.class.isAssignableFrom(dynaProperty.getContentType())) {
            	return dynaProperty;
            }     
            return new DynaProperty(dynaProperty.getName(),
            		ArooaValue.class, dynaProperty.getContentType());
        }

        /*
         *  (non-Javadoc)
         * @see org.apache.commons.beanutils.DynaClass#getDynaProperties()
         */
    	@Override
        public DynaProperty[] getDynaProperties() {
        	return delegate.getDynaProperties();
        }

        /*
         *  (non-Javadoc)
         * @see org.apache.commons.beanutils.DynaClass#getName()
         */
    	@Override
        public String getName() {
        	return delegate.getName();
        }    
        
        /*
         *  (non-Javadoc)
         * @see org.apache.commons.beanutils.DynaClass#newInstance()
         */
    	@Override
        public DynaBean newInstance() throws IllegalAccessException, InstantiationException {
        	throw new UnsupportedOperationException();
        }    

    	@Override
        public void add(String name) {
        	delegate.add(name);
        }
        
    	@Override
        public void add(String name, Class<?> type) {
        	delegate.add(name, type);
        }
        
    	@Override
        public void add(String name, Class<?> type, boolean readable,
        		boolean writeable) {
        	delegate.add(name, type, readable, writeable);
        }
        
    	@Override
        public boolean isRestricted() {
        	return delegate.isRestricted();
        }
        
    	@Override
        public void remove(String name) {
        	delegate.remove(name);
        }
        
    	@Override
        public void setRestricted(boolean restricted) {
        	delegate.setRestricted(restricted);
        }
    }
}

