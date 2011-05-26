package org.oddjob.values;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.beanutils.LazyDynaMap;
import org.apache.commons.beanutils.MutableDynaClass;
import org.oddjob.arooa.ArooaConstants;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;
import org.oddjob.framework.SimpleJob;


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
 * <pre>
 * &lt;oddjob&gt;
 *  &lt;job&gt;
 *   &lt;sequential&gt;
 *     &lt;variables id="vars" &gt;
 *      &lt;greeting&gt;
 *       &lt;value value="Hello World"/&gt;
 *      &lt;/greeting&gt;
 *     &lt;/variables&gt;
 *     &lt;echo name="Issue a greeting." text="${vars.greeting}" /&gt;
 *   &lt;/sequential&gt;
 *  &lt;/job&gt;
 * &lt;/oddjob&gt;
 * </pre>
 */

public class VariablesJob extends SimpleJob
		implements DynaBean {
	
	private final Map<String, Object> values = 
		new LinkedHashMap<String, Object>();
	
	/** The LazyDynaBean to delagate to. 
	 *  
	 *  For the refererence:
	 * 
	 * 
     * @oddjob.property <i>Any type</i>
     * @oddjob.description Any type.
     * @oddjob.required No.
	 * 
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
	protected int execute() throws Exception {
    	
		return 0;
	}

	@Override
	protected void onReset() {
		// Stop concurrent modification exception.
		List<String> keySet = new ArrayList<String>(values.keySet());
		
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
    public Object get(String name, String key) {
        return dynaBean.get(name, key);
    }

    /**
     * Return the <code>DynaClass</code> instance that describes the set of
     * properties available for this DynaBean.
     */
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
     * @see org.apche.commons.beanutils.LazyDynaBean
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
        public DynaProperty[] getDynaProperties() {
        	return delegate.getDynaProperties();
        }

        /*
         *  (non-Javadoc)
         * @see org.apache.commons.beanutils.DynaClass#getName()
         */
        public String getName() {
        	return delegate.getName();
        }    
        
        /*
         *  (non-Javadoc)
         * @see org.apache.commons.beanutils.DynaClass#newInstance()
         */
        public DynaBean newInstance() throws IllegalAccessException, InstantiationException {
        	throw new UnsupportedOperationException();
        }    

        public void add(String name) {
        	delegate.add(name);
        }
        
        public void add(String name, Class type) {
        	delegate.add(name, type);
        }
        
        public void add(String name, Class type, boolean readable,
        		boolean writeable) {
        	delegate.add(name, type, readable, writeable);
        }
        
        public boolean isRestricted() {
        	return delegate.isRestricted();
        }
        
        public void remove(String name) {
        	delegate.remove(name);
        }
        
        public void setRestricted(boolean restricted) {
        	delegate.setRestricted(restricted);
        }
    }

}

