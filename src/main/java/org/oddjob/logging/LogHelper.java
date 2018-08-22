/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper class which provides a method of getting
 * a logger for a given component.
 */
public class LogHelper {

    private static final Map<String, Integer> loggers 
    	= new HashMap<String, Integer>();
    
	/**
	 * Get a unique logger name for a component.
	 * 
	 * @param component The component.
	 * 
	 * @return A unique logger name;
	 */
    public static String uniqueLoggerName(Object component) {
		String className = component.getClass().getName();
		synchronized (loggers) {
			Integer count = (Integer) loggers.get(className);
			int c = 0;
			if (count != null) {
				c = count.intValue();
			}
			loggers.put(className, new Integer(c + 1));
			return className + "." + c;
		}		
	}
	
	/**
	 * Inspect the given component for a logger property.
	 * 
	 * @param component The component.
	 * @return The logger or null if one doesn't exist.
	 */
	public static String getLogger(Object component) {
		if (component instanceof LogEnabled) {
			return ((LogEnabled) component).loggerName();
		}
		return null;
//		try {
//			return (String) new BeanUtilsPropertyAccessor().getProperty(component, Reserved.LOGGER_PROPERTY);
//		} catch (Exception e) {
//			return null;
//		}
	}
}
