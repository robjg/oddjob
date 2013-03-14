/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.beanbus.mega;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.log4j.Logger;
import org.oddjob.Describeable;
import org.oddjob.Iconic;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ArooaLifeAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.TrackingBusListener;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.framework.ComponentBoundry;
import org.oddjob.framework.ComponentWrapper;
import org.oddjob.framework.WrapDynaBean;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.images.ImageIconStable;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogHelper;

/**
 * Wraps a Collection object so that it can be added to an 
 * {@link MegaBeanBus}. 
 * <p>
 * 
 * @author Rob Gordon.
 */
public class CollectionWrapper<E>
implements ComponentWrapper, ArooaSessionAware, DynaBean, BusPart, 
		LogEnabled, Describeable, Iconic, ArooaLifeAware, Collection<E> {
	
	public static final String INACTIVE = "inactive";
	
	public static final String ACTIVE = "active";
	
	public static final ImageIcon inactiveIcon
		= new ImageIconStable(
				IconHelper.class.getResource("diamond.gif"),
				"Inactive");
	
	public static final ImageIcon activeIcon
		= new ImageIconStable(
				IconHelper.class.getResource("dot_green.gif"),
				"Actvie");
	
	private static Map<String, ImageIcon> busPartIconMap = 
			new HashMap<String, ImageIcon>();

	static {
		busPartIconMap.put(INACTIVE, inactiveIcon);
		busPartIconMap.put(ACTIVE, activeIcon);
	}
	
    private Logger theLogger;
    
    private final Collection<E> wrapped;
    
    private final transient DynaBean dynaBean;
    
    private final Object proxy;
    
    private ArooaSession session;
    
    private final IconHelper iconHelper = new IconHelper(
    		this, INACTIVE, busPartIconMap);
   
    private final TrackingBusListener busListener = 
    		new TrackingBusListener() {
		
    	@Override
    	public void busCrashed(BusEvent event) {
    		busCrashException = event.getBusCrashException();
    	};
    	
		@Override
		public void busTerminated(BusEvent event) {
			iconHelper.changeIcon(INACTIVE);
		}
		
		@Override
		public void busStarting(BusEvent event) throws BusCrashException {
			busCrashException = null;
			iconHelper.changeIcon(ACTIVE);
		}
		
	};
    
	/** A job that isn't a bus service won't know the bus has crashed. */
	private volatile Exception busCrashException;
	
    /**
     * Constructor.
     * 
     * @param collection
     * @param proxy
     */
    public CollectionWrapper(Collection<E> collection, Object proxy) {
    	this.proxy = proxy;
        this.wrapped = collection;
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

	// 
	// Lifecycle Methods
	
	@Override
	public void initialised() {
	}
	
	@Override
	public void configured() {
	}
	
	@Override
	public void destroy() {
		busListener.setBusConductor(null);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.logging.LogEnabled#loggerName()
	 */
	@Override
    public String loggerName() {
		return logger().getName();    	
    }
    
	
	@Override
    public void prepare(BusConductor busConductor) {
    	
		ComponentBoundry.push(loggerName(), wrapped);
			try {
			
			busListener.setBusConductor(busConductor);
			
	    	this.session.getComponentPool().configure(getProxy());
	    	
			logger().info("Prepared with Bus Conductor [" + busConductor + "]");
			logger().info("In bus logging will be captured by the Bus Driver.");
			
		} finally {
			ComponentBoundry.pop();
		}
    }
        	
	/**
	 * Return an icon tip for a given id. Part
	 * of the Iconic interface.
	 */
	@Override
	public ImageIcon iconForId(String iconId) {
		return iconHelper.iconForId(iconId);
	}

	/**
	 * Add an icon listener. Part of the Iconic
	 * interface.
	 * 
	 * @param listener The listener.
	 */
	@Override
	public void addIconListener(IconListener listener) {
		iconHelper.addIconListener(listener);
	}

	/**
	 * Remove an icon listener. Part of the Iconic
	 * interface.
	 * 
	 * @param listener The listener.
	 */
	@Override
	public void removeIconListener(IconListener listener) {
		iconHelper.removeIconListener(listener);
	}
	
	// Object Methods
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		return other == getProxy();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
    public String toString() {
        return getWrapped().toString();
    }    
    
	@Override
    public boolean contains(String name, String key) {
    	return getDynaBean().contains(name, key);
    }
    
	@Override
    public Object get(String name) {
    	return getDynaBean().get(name);
    }

	@Override
    public Object get(String name, int index) {
    	return getDynaBean().get(name, index);
    }
    
	@Override
    public Object get(String name, String key) {
    	return getDynaBean().get(name, key);
    }
    
	@Override
    public DynaClass getDynaClass() {
    	return getDynaBean().getDynaClass();
    }
    
	@Override
    public void remove(String name, String key) {
    	getDynaBean().remove(name, key);
    }
    
	@Override
    public void set(String name, int index, Object value) {
    	getDynaBean().set(name, index, value);
    }
    
	@Override
    public void set(String name, Object value) {
    	getDynaBean().set(name, value);
    }
    
	@Override
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
	
	// Collection Methods
	//
	
	@Override
	public boolean add(E e) {
		if (busCrashException != null) {
			throw new RuntimeException(busCrashException);
		}
		return wrapped.add(e);
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		if (busCrashException != null) {
			throw new RuntimeException(busCrashException);
		}
		return wrapped.addAll(c);
	}
	
	@Override
	public void clear() {
		wrapped.clear();
	}
	
	@Override
	public boolean contains(Object o) {
		return wrapped.contains(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return wrapped.containsAll(c);
	}
	
	@Override
	public boolean isEmpty() {
		return wrapped.isEmpty();
	}
	
	@Override
	public Iterator<E> iterator() {
		return wrapped.iterator();
	}
	
	@Override
	public boolean remove(Object o) {
		return wrapped.remove(o);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		return wrapped.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		return wrapped.retainAll(c);
	}
	
	@Override
	public int size() {
		return wrapped.size();
	}
	
	@Override
	public Object[] toArray() {
		return wrapped.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return wrapped.toArray(a);
	}
}
