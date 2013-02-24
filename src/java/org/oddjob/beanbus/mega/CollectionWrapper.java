/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.beanbus.mega;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.log4j.Logger;
import org.oddjob.Describeable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.framework.ComponentWrapper;
import org.oddjob.framework.WrapDynaBean;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogHelper;

/**
 * Wraps a Service object and adds state to it. 
 * <p>
 * 
 * @author Rob Gordon.
 */
public class CollectionWrapper
implements ComponentWrapper, ArooaSessionAware, DynaBean, BusPart, 
		LogEnabled, Describeable {
	
    private transient Logger theLogger;
    
    private Collection<?> wrapped;
    
    private transient DynaBean dynaBean;
    
    private final Object proxy;
    	
    private ArooaSession session;
    
    public CollectionWrapper(Collection<?> service, Object proxy) {
    	this.proxy = proxy;
        this.wrapped = service;
        this.dynaBean = new WrapDynaBean(wrapped);    	
    }

    @Override
    public void setArooaSession(ArooaSession session) {
    	this.session = session;
    }
    
	protected Object getWrapped() {
		return wrapped;
	}

	protected DynaBean getDynaBean() {
		return dynaBean;
	}

	protected Object getProxy() {
		return proxy;
	}

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
			theLogger = Logger.getLogger(logger);
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
    
	@Override
    public void prepare() {
    	
    	this.session.getComponentPool().configure(getProxy());
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
        return getWrapped().getClass().getSimpleName();
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
    
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.Describeable#describe()
	 */
	@Override
	public Map<String, String> describe() {
		return new UniversalDescriber(session).describe(
				getWrapped());
	}
}
