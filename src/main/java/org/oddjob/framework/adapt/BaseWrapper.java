/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework.adapt;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.oddjob.*;
import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.framework.extend.BaseComponent;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.framework.util.StopWait;
import org.oddjob.images.IconHelper;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogHelper;
import org.oddjob.state.IsStoppable;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base class for proxy creators.
 *
 */
abstract public class BaseWrapper extends BaseComponent 
implements Runnable, Stateful, Resettable, DynaBean, Stoppable,
		LogEnabled, Describable {
    
    private transient Logger theLogger;
    
    /**
     * Return the object that is being proxied.
     * 
     * @return The component being proxied.
     */
	abstract protected Object getWrapped();
	
	/**
	 * Sub classes must provide a dyna bean for properties.
	 * 
	 * @return
	 */
	abstract protected DynaBean getDynaBean();
	
	/**
	 * Subclass must provide the proxy.
	 * 
	 * @return
	 */
	abstract protected Object getProxy();
	    
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.framework.BaseComponent#logger()
	 */
	protected Logger logger() {
    	if (theLogger == null) {
    		String logger = LogHelper.getLogger(getWrapped());
    		if (logger == null) {
    			logger = LogHelper.uniqueLoggerName(getWrapped());
    		}
			theLogger = LoggerFactory.getLogger(logger);
    	}
    	return theLogger;
    }

	/*
	 * (non-Javadoc)
	 * @see org.oddjob.logging.LogEnabled#loggerName()
	 */
    public String loggerName() {
		return logger().getName();    	
    }
    
    /**
     * Called by sub classes to configure the component.
     * 
     * @throws ArooaConfigurationException
     */
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
	 *
	 * Note we don't override hashCode on proxy because it would call back
	 * to this wrapper so just let the native hash code provide the contract
	 * as there is one to one between this wrapper and its proxy.
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
		
		try (Restore ignored = ComponentBoundary.push(loggerName(), this)) {
			final AtomicReference<String> icon = new AtomicReference<>();
			
	    	if (!stateHandler().waitToWhen(new IsStoppable(), () -> {
                logger().info("Stop requested.");
                icon.set(iconHelper().currentId());
                iconHelper().changeIcon(IconHelper.STOPPING);
            })) {
	    		return;
	    	}
	    	
	    	FailedToStopException failedToStopException = null;
	    	
			try {
				onStop();
				
				new StopWait(this).run();
				
				logger().info("Stopped.");
			} 
			catch (RuntimeException e) {
				failedToStopException = new FailedToStopException(this, e);
			}
			catch (FailedToStopException e) {
				failedToStopException = e;
			}
			
			if (failedToStopException != null) {
				
				stateHandler().waitToWhen(
				        new IsStoppable(),
                                          () -> iconHelper().changeIcon(icon.get()));
				throw failedToStopException;
			}
		} 
	}
	
	protected void onStop() throws FailedToStopException {} 
	
	/**
	 * Get the result. Use either the return value from the Callable or
	 * the result property if there is one.
	 *
     * @return The result.
     *
	 * @throws ArooaConversionException If the result can't be converted to int.
	 * @throws ArooaPropertyException If the result property can't be read.
	 */
	protected int getResult(Object callableResult)
            throws ArooaPropertyException, ArooaConversionException {

		ArooaSession session = getArooaSession();
		if (session == null) {
			// Must be running outside Oddjob from code.
			return 0;
		}
		
		Integer result;
		if (callableResult != null) {
			result = session.getTools().getArooaConverter().convert(
					callableResult, Integer.class);
		}
		else {
			PropertyAccessor accessor = session.getTools().getPropertyAccessor();
			BeanOverview overview = accessor.getBeanOverview(
					getWrapped().getClass()); 
			
			if (!overview.hasReadableProperty(
							Reserved.RESULT_PROPERTY)) {
				return 0;
			}
			
			ArooaConverter converter = session.getTools().getArooaConverter();
			
			result = converter.convert(accessor.getProperty(
					getWrapped(), Reserved.RESULT_PROPERTY), 
					Integer.class);
		}
		
		if (result == null) {
			return 0;
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.Describeable#describe()
	 */
	@Override
	public Map<String, String> describe() {
		return new UniversalDescriber(getArooaSession()).describe(
				getWrapped());
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		try {
			stop();
		} catch (FailedToStopException e) {
			logger().warn("Failed to stop during destroy.", e);
		}
	}
	
	/**
	 * Helper class to find interfaces implemented by an object.
	 * 
	 * @param object
	 * 
	 * @return An array of the interface classes.
	 */
    public static Class<?>[] interfacesFor(Object object) {
    	List<Class<?>> results = new ArrayList<Class<?>>();
    	for (Class<?> cl = object.getClass(); cl != null; cl = cl.getSuperclass()) {
    		results.addAll(Arrays.asList((Class<?>[]) cl.getInterfaces()));
    	}
    	return (Class[]) results.toArray(new Class[results.size()]);
    }
    
}
