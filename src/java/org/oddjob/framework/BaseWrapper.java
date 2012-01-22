/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Reserved;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.images.IconHelper;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogHelper;
import org.oddjob.state.IsStoppable;

/**
 * Base class for proxy creators.
 *
 */
abstract public class BaseWrapper extends BaseComponent 
implements Runnable, Stateful, Resetable, DynaBean, Stoppable, 
		LogEnabled {
    
    private transient Logger theLogger;
    
    /**
     * Return the object that is being proxied.
     * 
     * @return The component being proxied.
     */
	abstract public Object getWrapped();
	
	/**
	 * 
	 * @return
	 */
	abstract protected DynaBean getDynaBean();
	
	abstract protected Object getProxy();
	    
	protected Logger logger() {
    	if (theLogger == null) {
    		String logger = LogHelper.getLogger(getWrapped());
    		if (logger == null) {
    			logger = LogHelper.uniqueLoggerName(getWrapped());
    		}
			theLogger = Logger.getLogger(logger);
    	}
    	return theLogger;
    }

    public String loggerName() {
		return logger().getName();    	
    }
    
	protected void configure() 
	throws ArooaConfigurationException {
		configure(getProxy());
	}
	
	protected void save() throws ComponentPersistException {
		save(getProxy());
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		return other == getProxy();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    public String toString() {
        return getWrapped().toString();
    }    
    
    public boolean contains(String name, String key) {
    	return getDynaBean().contains(name, key);
    }
    
    public Object get(String name) {
    	return getDynaBean().get(name);
    }

    public Object get(String name, int index) {
    	return getDynaBean().get(name, index);
    }
    
    public Object get(String name, String key) {
    	return getDynaBean().get(name, key);
    }
    
    public DynaClass getDynaClass() {
    	return getDynaBean().getDynaClass();
    }
    
    public void remove(String name, String key) {
    	getDynaBean().remove(name, key);
    }
    
    public void set(String name, int index, Object value) {
    	getDynaBean().set(name, index, value);
    }
    
    public void set(String name, Object value) {
    	getDynaBean().set(name, value);
    }
    
    public void set(String name, String key, Object value) {
    	getDynaBean().set(name, key, value);
    }
    
	public final void stop() throws FailedToStopException {
		stateHandler().assertAlive();
		
		ComponentBoundry.push(loggerName(), this);
		try {
	    	if (!stateHandler().waitToWhen(new IsStoppable(), new Runnable() {
	    		public void run() {    			
	    		}
	    	})) {
	    		return;
	    	}
	    	
			logger().info("Stop requested.");
			
			String icon = iconHelper.currentId();
			iconHelper.changeIcon(IconHelper.STOPPING);
			try {
				onStop();
				
				new StopWait(this).run();
				
				logger().info("Stopped.");
				
			} catch (RuntimeException e) {
				iconHelper.changeIcon(icon);
				throw e;
			}
			catch (FailedToStopException e) {
				iconHelper.changeIcon(icon);
				throw e;
			}
		} finally {
			ComponentBoundry.pop();
		}
	}
	
	protected void onStop() throws FailedToStopException {} 
	
	/**
	 * 
	 * @return
	 */
	protected int getResult() {
		try {
			Integer result = (Integer) PropertyUtils.getProperty(
					getWrapped(), Reserved.RESULT_PROPERTY);
			return result.intValue();
		}
		catch (Exception e) {
			return 0;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		try {
			stop();
		} catch (FailedToStopException e) {
			logger().warn(e);
		}
	}
	
    public static Class<?>[] interfacesFor(Object object) {
    	List<Class<?>> results = new ArrayList<Class<?>>();
    	for (Class<?> cl = object.getClass(); cl != null; cl = cl.getSuperclass()) {
    		results.addAll(Arrays.asList((Class<?>[]) cl.getInterfaces()));
    	}
    	return (Class[]) results.toArray(new Class[0]);
    }
    
}
