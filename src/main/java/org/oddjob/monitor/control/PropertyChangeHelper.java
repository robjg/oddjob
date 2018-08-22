/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.control;

import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Checks beans to see if they support property change listeners.
 */
public class PropertyChangeHelper {

	private static final Logger logger = LoggerFactory.getLogger(PropertyChangeHelper.class);

	private static final Map<Class<?>, PropertyChangeHelper> helpers = 
		new HashMap<Class<?>, PropertyChangeHelper>();

	/** Remember the addListener method. */
	private Method addPropListenerMethod;

	/** Remember the removeListener method. */
	private Method removePropListenerMethod;

	private PropertyChangeHelper(Class<?> bean) {
		Class<?> beanClass = bean.getClass();
		Class<?>[] argClasses = { PropertyChangeListener.class };
		try {
			addPropListenerMethod = beanClass.getMethod(
					"addPropertyChangeListener", argClasses);

			removePropListenerMethod = beanClass.getMethod(
					"removePropertyChangeListener", argClasses);
		} catch (SecurityException e) {
			logger.debug("Failed getting methods.", e);
		} catch (NoSuchMethodException e) {
			// ignore
		}
	}

	private static PropertyChangeHelper lookup(Class<?> bean) {
		synchronized (helpers) {
			PropertyChangeHelper helper = helpers.get(bean);
			if (helper == null) {
				helper = new PropertyChangeHelper(bean);
				helpers.put(bean, helper);
			}
			return helper;
		}
	}

	public static void addPropertyChangeListener(Object obj,
			PropertyChangeListener l) {
		PropertyChangeHelper helper = lookup(obj.getClass());
		if (helper.addPropListenerMethod == null) {
			return;
		}
		Object[] args = { obj };
		try {
			helper.addPropListenerMethod.invoke(obj, args);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			logger.debug("Failed adding property listener.", e);
		}
	}

	public static void removePropertyChangeListener(Object obj,
			PropertyChangeListener l) {
		PropertyChangeHelper helper = lookup(obj.getClass());
		if (helper.removePropListenerMethod == null) {
			return;
		}
		Object[] args = { obj };
		try {
			helper.removePropListenerMethod.invoke(obj, args);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			logger.debug("Failed to remove property listener.", e);
		}
	}
}
