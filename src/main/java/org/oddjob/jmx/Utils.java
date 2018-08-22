/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx;

import java.io.NotSerializableException;
import java.io.Serializable;

import org.oddjob.framework.Exportable;
import org.oddjob.framework.Transportable;

/**
 * Utilities mainly for method argument manipulation during
 * remote jmx calls.
 */
public class Utils {
	
	/**
	 * Convert an array of classes to an array of strings for invoking a
	 * JMX operation.
	 * 
	 * @param classes Array of classes.
	 * @return Array of Strings.
	 */
	public static String[] classArray2StringArray(Class<?>[] classes) {
		String[] strings = new String[classes.length];
		for (int i = 0; i < classes.length; ++i) {
			strings[i] = classes[i].getName();
		}
		return strings;
	}
	
	/**
	 * Convert an array of objects to Objects that can
	 * be sent accross the wire in a remote method call.
	 * 
	 * @param objects
	 * @return
	 * @throws NotSerializableException
	 */
	public static Serializable[] export(Object[] objects) 
	throws NotSerializableException {
		if (objects == null) {
			return null;
		}
		Serializable[] results = new Serializable[objects.length];
		for (int i = 0; i < objects.length; ++i) {
			results[i] = export(objects[i]);
		}
		return results;
	}

	/**
	 * Convert an object into something that can be sent accross the wire.
	 * 
	 * @param object
	 * @return
	 * @throws NotSerializableException
	 */
	public static Serializable export(Object object) 
	throws NotSerializableException {
		if (object == null) {
			return null;
		}
		if (object instanceof Exportable) {
			return ((Exportable) object).exportTransportable();
		}
		else if (object instanceof Serializable){
			return (Serializable) object;
		}
		else {
			throw new NotSerializableException(object.getClass().getName());
		}
	}

	/**
	 * Import an array of objects that have come accross the wire.
	 * 
	 * @param objects
	 * @param componentRegistry
	 * @return
	 */
	public static Object[] importResolve(Object[] objects, ObjectNames names) {
		if (objects == null) {
			return null;
		}
		Object[] results = new Object[objects.length];
		for (int i = 0; i < objects.length; ++i) {
			results[i] = importResolve(objects[i], names);
		}
		return results;
	}
	
	/**
	 * Import an object that has come across the wire.
	 * 
	 * @param object
	 * @param componentRegistry
	 * @return
	 */
	public static Object importResolve(Object object, ObjectNames names) {
		if (object == null) {
			return null;
		}
		if (object instanceof Transportable) {
			return ((Transportable) object).importResolve(names);
		}
		else {
			return object;
		}
	}
		
}
