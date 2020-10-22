/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.beanbus.mega;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.oddjob.Describable;
import org.oddjob.Iconic;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ArooaLifeAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.beanbus.*;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.framework.adapt.ComponentWrapper;
import org.oddjob.framework.adapt.beanutil.WrapDynaBean;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.images.ImageData;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogHelper;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOError;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Wraps a Collection object so that it can be added to an 
 * {@link MegaBeanBus}. 
 * <p>
 * 
 * @author Rob Gordon.
 */
public class CollectionWrapper<E>
implements ComponentWrapper, ArooaSessionAware, DynaBean, BusPart, 
		LogEnabled, Describable, Iconic, ArooaLifeAware, Collection<E> {
	
	public static final String INACTIVE = "inactive";
	
	public static final String ACTIVE = "active";
	
	public static final ImageData inactiveIcon;

	public static final ImageData activeIcon;

	static {
		try {
			inactiveIcon = ImageData.fromUrl(
					IconHelper.class.getResource("diamond.gif"),
					"Inactive");

			activeIcon = ImageData.fromUrl(
					IconHelper.class.getResource("dot_green.gif"),
					"Actvie");
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	private static final Map<String, ImageData> busPartIconMap =
			new HashMap<>();

	static {
		busPartIconMap.put(INACTIVE, inactiveIcon);
		busPartIconMap.put(ACTIVE, activeIcon);
	}
	
    private volatile Logger theLogger;
    
    private final Collection<E> wrapped;
    
    private final transient DynaBean dynaBean;
    
    private final Object proxy;
    
    private volatile ArooaSession session;
    
    private final IconHelper iconHelper = new IconHelper(
    		this, INACTIVE, busPartIconMap);
   
    private final TrackingBusListener busListener = 
    		new TrackingBusListener() {
		
    	@Override
    	public void busCrashed(BusEvent event) {
    		busCrashException = event.getBusCrashException();
    	}

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
			theLogger = LoggerFactory.getLogger(logger);
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
    	
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			
			busListener.setBusConductor(busConductor);
			
	    	this.session.getComponentPool().configure(getProxy());
	    	
			logger().info("Prepared with Bus Conductor [" + busConductor + "]");
			
		}
    }
        	
	@Override
	public BusConductor conductorForService(BusConductor busConductor) {
		return new LoggingBusConductorFilter(busConductor);
	}
	
	/**
	 * Return an icon tip for a given id. Part
	 * of the Iconic interface.
	 */
	@Override
	public ImageData iconForId(String iconId) {
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
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			return wrapped.add(e);
		}
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		if (busCrashException != null) {
			throw new RuntimeException(busCrashException);
		}
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			return wrapped.addAll(c);
		}
	}
	
	@Override
	public void clear() {
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			wrapped.clear();
		}
	}
	
	@Override
	public boolean contains(Object o) {
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			return wrapped.contains(o);
		}
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			return wrapped.containsAll(c);
		}
	}
	
	@Override
	public boolean isEmpty() {
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			return wrapped.isEmpty();
		}
	}
	
	@Override
	public Iterator<E> iterator() {
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			return wrapped.iterator();
		}
	}
	
	@Override
	public boolean remove(Object o) {
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			return wrapped.remove(o);
		}
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			return wrapped.removeAll(c);
		}
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			return wrapped.retainAll(c);
		}
	}
	
	@Override
	public int size() {
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			return wrapped.size();
		}
	}
	
	@Override
	public Object[] toArray() {
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			return wrapped.toArray();
		}
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
			return wrapped.toArray(a);
		}
	}
	
	class LoggingBusConductorFilter extends BusConductorFilter {
		
		public LoggingBusConductorFilter(BusConductor conductor) {
			super(conductor);
		}
		
		@Override
		protected void busStarting(BusEvent event,
				BusListener listener) throws BusCrashException {
			try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
				super.busStarting(event, listener);
			}
		}
		
		@Override
		protected void tripBeginning(BusEvent event,
				BusListener listener) throws BusCrashException {
			try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
				super.tripBeginning(event, listener);
			}
		}
		
		@Override
		protected void tripEnding(BusEvent event,
				BusListener listener) throws BusCrashException {
			try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
				super.tripEnding(event, listener);
			}
		}
		
		@Override
		protected void busStopRequested(BusEvent event,
				BusListener listener) {
			try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
				super.busStopRequested(event, listener);
			}
		}
		
		@Override
		protected void busStopping(BusEvent event,
				BusListener listener) throws BusCrashException {
			try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
				super.busStopping(event, listener);
			}
		}
		
		@Override
		protected void busCrashed(BusEvent event,
				BusListener listener) {
			try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
				super.busCrashed(event, listener);
			}
		}
		
		@Override
		protected void busTerminated(BusEvent event,
				BusListener listener) {
			try (Restore restore = ComponentBoundary.push(loggerName(), wrapped)) {
				super.busTerminated(event, listener);
			}
		}
		
	}
}
