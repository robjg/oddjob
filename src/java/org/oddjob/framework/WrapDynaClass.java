/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.oddjob.framework;


import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;


/**
 * <p>Implementation of <code>DynaClass</code> for DynaBeans that wrap
 * standard JavaBean instances.</p>
 * <p>
 * Based on the BeanUtils version but Serializable, and also supports
 * mapped types.
 *
 * @author Rob Gorodon, based on the original from BeanUtils.
 */

public class WrapDynaClass implements DynaClass, Serializable {
	private static final long serialVersionUID = 20051114;

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new WrapDynaClass for the specified JavaBean class.  This
     * constructor is private; WrapDynaClass instances will be created as
     * needed via calls to the <code>createDynaClass(Class)</code> method.
     *
     * @param beanClass JavaBean class to be introspected around
     */
    private WrapDynaClass(Class<?> beanClass) {

        this.beanClassName = beanClass.getName();
        introspect(beanClass);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The JavaBean <code>Class</code> which is represented by this
     * <code>WrapDynaClass</code>.
     */
    private final String beanClassName;

    /**
     * The set of dynamic properties that are part of this DynaClass.
     */
    private DynaProperty properties[] = null;


    /**
     * The set of dynamic properties that are part of this DynaClass,
     * keyed by the property name.  Individual descriptor instances will
     * be the same instances as those in the <code>properties</code> list.
     */
    private HashMap<String, DynaProperty> propertiesMap = 
    	new HashMap<String, DynaProperty>();

    private Set<String> readableProperties = new HashSet<String>();
    
    private Set<String> writableProperties = new HashSet<String>();

    // ------------------------------------------------------- Static Variables


    /**
     * The set of <code>WrapDynaClass</code> instances that have ever been
     * created, keyed by the underlying bean Class.
     */
    private static HashMap<Class<?>, WrapDynaClass> dynaClasses = 
    	new HashMap<Class<?>, WrapDynaClass>();


    // ------------------------------------------------------ DynaClass Methods


    /**
     * Return the name of this DynaClass (analogous to the
     * <code>getName()</code> method of <code>java.lang.Class</code), which
     * allows the same <code>DynaClass</code> implementation class to support
     * different dynamic classes, with different sets of properties.
     */
    public String getName() {
        return (this.beanClassName);
    }


    /**
     * Return a property descriptor for the specified property, if it exists;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the dynamic property for which a descriptor
     *  is requested
     *
     * @exception IllegalArgumentException if no property name is specified
     */
    public DynaProperty getDynaProperty(String name) {
        if (name == null) {
            throw new IllegalArgumentException
                    ("No property name specified");
        }
        return ((DynaProperty) propertiesMap.get(name));
    }


    /**
     * <p>Return an array of <code>ProperyDescriptors</code> for the properties
     * currently defined in this DynaClass.  If no properties are defined, a
     * zero-length array will be returned.</p>
     *
     * <p><strong>FIXME</strong> - Should we really be implementing
     * <code>getBeanInfo()</code> instead, which returns property descriptors
     * and a bunch of other stuff?</p>
     */
    public DynaProperty[] getDynaProperties() {
        return (properties);
    }

    /**
     * Unsupported.
     * 
     * @throws UnsupportedOperationException Always.
     */
    public DynaBean newInstance()
            throws UnsupportedOperationException {
    	throw new UnsupportedOperationException("Can't create bean instances with this DynaClass.");
    }

    public boolean isReadable(String propertyName) {
    	if (!propertiesMap.containsKey(propertyName)) {
    		throw new IllegalArgumentException("No such property " + propertyName);
    	}
    	return readableProperties.contains(propertyName);
    }
    
    public boolean isWritable(String propertyName) {
    	if (!propertiesMap.containsKey(propertyName)) {
    		throw new IllegalArgumentException("No such property " + propertyName);
    	}
    	return writableProperties.contains(propertyName);
    }

    // --------------------------------------------------------- Static Methods


    /**
     * Clear our cache of WrapDynaClass instances.
     */
    public static void clear() {
        synchronized (dynaClasses) {
            dynaClasses.clear();
        }
    }


    /**
     * Create (if necessary) and return a new <code>WrapDynaClass</code>
     * instance for the specified bean class.
     *
     * @param beanClass Bean class for which a WrapDynaClass is requested
     */
    public static WrapDynaClass createDynaClass(Class<?> beanClass) {

        synchronized (dynaClasses) {
            WrapDynaClass dynaClass =
                    (WrapDynaClass) dynaClasses.get(beanClass);
            if (dynaClass == null) {
                dynaClass = new WrapDynaClass(beanClass);
                dynaClasses.put(beanClass, dynaClass);
            }
            return (dynaClass);
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Introspect our bean class to identify the supported properties.
     */
    protected void introspect(Class<?> beanClass) {
    	Set<String> mismatched = new HashSet<String>();
    	
    	// first find simple and indexed properties via usual means.
    	PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(beanClass);
    	for (int i = 0; i < descriptors.length; ++i) {
    		PropertyDescriptor descriptor = descriptors[i];
    		
    		String propertyName = descriptor.getName();
    		
    		DynaProperty dynaProperty;
    		// indexed property?
    		if (descriptor instanceof IndexedPropertyDescriptor) {
    			dynaProperty = new DynaProperty(propertyName, 
    					descriptor.getPropertyType(),
    					((IndexedPropertyDescriptor) descriptor).getIndexedPropertyType());
    		}
    		// else a simple property.
    		else {
    			dynaProperty = new DynaProperty(propertyName,
    					descriptor.getPropertyType());
    		}
    		
    		propertiesMap.put(propertyName, dynaProperty);
    		
    		// update readable writable
    		if (MethodUtils.getAccessibleMethod(descriptor.getReadMethod()) != null) {
    			readableProperties.add(propertyName);
    		}
    		if (MethodUtils.getAccessibleMethod(descriptor.getWriteMethod()) != null) {
    			writableProperties.add(propertyName);
    		}
    	}
    	
    	// now find mapped properties.
    	Method[] methods = beanClass.getMethods();
    	for (int i = 0; i < methods.length; ++i) {
    		Method method = methods[i];

    		// methods beginning with get could be properties.    		
    		if (!method.getName().startsWith("get")
    				&& !method.getName().startsWith("set")) {
    			continue;
    		}
    		
    		String propertyName = method.getName().substring(3);
    		// get on it's own is not a property
    		if (propertyName.length() == 0) {
    			continue;
    		}
    		
    		// lowercase first letter
    		propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
    		
    		Class<?>[] args = method.getParameterTypes();

    		DynaProperty dynaProperty = null;

    		boolean readable = false;
    		boolean writable = false;
    		
    		// is mapped property?
    		if (method.getName().startsWith("get")
    				&& Void.TYPE != method.getReturnType()
    				&& args.length == 1
    				&& args[0] == String.class) {
    			DynaProperty existing = (DynaProperty) propertiesMap.get(propertyName);
    			if (existing != null && !existing.isMapped()) {
    				mismatched.add(propertyName);
    				continue;
    			}
    			dynaProperty = new DynaProperty(propertyName,
    					Map.class,
    					method.getReturnType());
    			readable = true;
    		}
    		else if (args.length == 2
    				&& args[0] == String.class
    				&& Void.TYPE == method.getReturnType()) {
    			DynaProperty existing = (DynaProperty) propertiesMap.get(propertyName);
    			if (existing != null && !existing.isMapped()) {
    				mismatched.add(propertyName);
    				continue;
    			}
    			dynaProperty = new DynaProperty(propertyName,
    					Map.class,
    					args[1]);
    			writable = true;
    		}
    		else {
    			continue;
    		}
			propertiesMap.put(propertyName, dynaProperty);

    		// update readable writable
    		if (readable) {
    			readableProperties.add(propertyName);
    		}    		
    		if (writable) {
    			writableProperties.add(propertyName);
    		}
    	}
    	
    	for (String element : mismatched) {
	    	propertiesMap.remove(element);
	    	readableProperties.remove(element);
	    	writableProperties.remove(element);
		}

    	properties = (DynaProperty[]) propertiesMap.values().toArray(new DynaProperty[0]);
    	
    }
    
	/*
	 * Custom serialization.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
	}
	
	/*
	 * Custom serialization.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
	}

}
