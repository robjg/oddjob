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

package org.oddjob.framework.adapt.beanutil;


import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;


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


    // ------------------------------------------------------- Static Variables


    /**
     * The set of <code>WrapDynaClass</code> instances that have ever been
     * created, keyed by the underlying bean Class.
     */
    private static final HashMap<Class<?>, WrapDynaClass> dynaClasses =
            new HashMap<>();

    // ----------------------------------------------------- Instance Variables


    /**
     * The JavaBean <code>Class</code> which is represented by this
     * <code>WrapDynaClass</code>.
     */
    private final String beanClassName;

    /**
     * The set of dynamic properties that are part of this DynaClass.
     */
    private final DynaProperty[] properties;

    /**
     * The set of dynamic properties that are part of this DynaClass,
     * keyed by the property name.  Individual descriptor instances will
     * be the same instances as those in the <code>properties</code> list.
     */
    private final HashMap<String, DynaProperty> propertiesMap;

    private final Set<String> readableProperties;

    private final Set<String> writableProperties;


    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new WrapDynaClass for the specified JavaBean class.  This
     * constructor is private; WrapDynaClass instances will be created as
     * needed via calls to the <code>createDynaClass(Class)</code> method.
     *
     * @param beanClass          JavaBean class to be introspected around
     * @param propertiesMap      Property to DynaProperty map
     * @param readableProperties Readable properties
     * @param writableProperties Writable properties.
     */
    private WrapDynaClass(Class<?> beanClass,
                          HashMap<String, DynaProperty> propertiesMap,
                          Set<String> readableProperties,
                          Set<String> writableProperties) {

        this.beanClassName = beanClass.getName();

        this.properties = propertiesMap.values().toArray(new DynaProperty[0]);
        this.propertiesMap = propertiesMap;
        this.readableProperties = readableProperties;
        this.writableProperties = writableProperties;
    }

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
     *             is requested
     * @throws IllegalArgumentException if no property name is specified
     */
    public DynaProperty getDynaProperty(String name) {
        if (name == null) {
            throw new IllegalArgumentException
                    ("No property name specified");
        }
        return propertiesMap.get(name);
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
    public static WrapDynaClass createDynaClass(Class<?> beanClass, PropertyAccessor propertyAccessor) {

        synchronized (dynaClasses) {
            WrapDynaClass dynaClass =
                    (WrapDynaClass) dynaClasses.get(beanClass);
            if (dynaClass == null) {
                dynaClass = introspect(beanClass, propertyAccessor);
                dynaClasses.put(beanClass, dynaClass);
            }
            return (dynaClass);
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Introspect our bean class to identify the supported properties.
     */
    protected static WrapDynaClass introspect(Class<?> beanClass, PropertyAccessor propertyAccessor) {

        BeanOverview beanOverview = propertyAccessor.getBeanOverview(beanClass);
        String[] propertyNames = beanOverview.getProperties();

        final HashMap<String, DynaProperty> propertiesMap = new HashMap<>(propertyNames.length);
        final Set<String> readableProperties = new HashSet<>(propertyNames.length);
        final Set<String> writableProperties = new HashSet<>(propertyNames.length);

        for (String propertyName : propertyNames) {

            DynaProperty dynaProperty;

            // indexed
            if (beanOverview.isIndexed(propertyName)) {
                dynaProperty = new DynaProperty(propertyName,
                        List.class,
                        beanOverview.getPropertyType(propertyName));
                // mapped
            } else if (beanOverview.isMapped(propertyName)) {
                dynaProperty = new DynaProperty(propertyName,
                        Map.class,
                        beanOverview.getPropertyType(propertyName));
            } else {
                // else a simple property.
                dynaProperty = new DynaProperty(propertyName,
                        beanOverview.getPropertyType(propertyName));
            }

            propertiesMap.put(propertyName, dynaProperty);

            // update readable writable
            if (beanOverview.hasReadableProperty(propertyName)) {
                readableProperties.add(propertyName);
            }
            if (beanOverview.hasWriteableProperty(propertyName)) {
                writableProperties.add(propertyName);
            }
        }

        return new WrapDynaClass(beanClass, propertiesMap, readableProperties, writableProperties);
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
