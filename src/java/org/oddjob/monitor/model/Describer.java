/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.log4j.Logger;
import org.oddjob.Describeable;
import org.oddjob.Reserved;

/**
 * Helper to describe the properties of a component for use in
 * monitors.
 *
 * @author Rob Gordon.
 */
public class Describer {
	private static final Logger logger = Logger.getLogger(Describer.class);
	
	/**
	 * Describe the component. The component could be a
	 * remote proxy which is a DynaBean that does have
	 * a description property, or a local DyanBean such as
	 * variables which doesn't have a description property
	 * or a normal bean.
	 * 
	 * @param bean The component.
	 * @return A map of the descriptions.
	 */
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> describe(Object bean) {
		if (bean == null) {
			throw new NullPointerException("Component must not be null.");
		}
		
		Map<String, String> description = null;
		
		if (bean instanceof Describeable) {
			return ((Describeable) bean).describe();
		}
		
		try {
			Method m = bean.getClass().getMethod(Reserved.DESCRIBE_METHOD, new Class[0]);
			description = (Map<String, String>) m.invoke(bean, new Object[0]);
		} catch (Exception exception) {
			// ignore
		}
		if (description != null) {
			return description;
		}

		// this bit of code is straight out of BeanUtilsBean but
		// it copes with mapped properties
	    if (bean instanceof DynaBean) {
	    	description = new HashMap<String, String>();
            DynaProperty descriptors[] =
                ((DynaBean) bean).getDynaClass().getDynaProperties();
            for (int i = 0; i < descriptors.length; i++) {
                String name = descriptors[i].getName();
            	if (descriptors[i].isMapped()) {
            		continue;
            	}
            	if (descriptors[i].isIndexed()) {
            		continue;
            	}
        		try {
        			description.put(name, BeanUtilsBean.getInstance().getProperty(bean, name));
        		} catch (InvocationTargetException e) {
        			logger.debug("Failed to describe:", e);
        			Throwable t = e.getTargetException();
        			description.put(name + " (Error)", t.toString());
        		} catch (Exception e) {
        			logger.debug("Failed to describe:", e);
        			description.put(name + "Failed)", e.toString());
        		}
            }
		    return (description);
		} else {
			try {
		    	return BeanUtilsBean.getInstance().describe(bean);
			} catch (InvocationTargetException e) {
				logger.debug("Failed to describe:", e);
				Throwable t = e.getTargetException();
				Map<String, String> map = new HashMap<String, String>();
				map.put("Error", t.toString());
				return map;
			} catch (Exception e) {
				logger.debug("Failed to describe:", e);
				Map<String, String> map = new HashMap<String, String>();
				map.put("Failed to describe:", e.getMessage());
				return map;
			}
		}
	}
}
