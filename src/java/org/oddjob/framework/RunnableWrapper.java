/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.FailedToStopException;
import org.oddjob.Iconic;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.images.StateIcons;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;

/**
 * Creates a proxy for any java.lang.Runnable to allow it to be controlled and
 * monitored withing Oddjob.
 * 
 * @author Rob Gordon.
 */
public class RunnableWrapper extends BaseWrapper implements InvocationHandler,
		Serializable {
	private static final long serialVersionUID = 20051231;

	/** The wrapped Runnable. */
	private Runnable wrapped;

	/**
	 * The DynaBean that takes its properties of the wrapped Runnable.
	 */
	private transient DynaBean dynaBean;

	/** The thread our job is executing on. */
	private volatile transient Thread thread;

	/**
	 * Map of methods to the object that it will be invoked on.
	 */
	private transient Map<Method, Object> methods;

	/**
	 * The proxy we create that represents our wrapped Runnable within Oddjob.
	 */
	private Object proxy;

	/**
	 * Private constructor. The wrapped should always be accessed using the
	 * static {@link #wrapperFor(Runnable)} method.
	 * 
	 */
	private RunnableWrapper(Runnable wrapped) {
		completeConstruction();
		setWrapped(wrapped);
	}

	private void completeConstruction() {
	}
	
	/**
	 * Create the proxy that oddjob will use to communicate wiith our Runnable.
	 * 
	 * @param wrapped
	 * @return
	 */
	public static Runnable wrapperFor(Runnable wrapped, 
			ClassLoader classLoader) {
    	
    	RunnableWrapper wrapper = new RunnableWrapper(wrapped);
    	
    	Set<Class<?>> interfaces = new HashSet<Class<?>>();
    	interfaces.addAll(Arrays.asList(interfacesFor(wrapped)));
    	interfaces.add(ArooaContextAware.class);
    	interfaces.add(Stateful.class);
    	interfaces.add(Resetable.class);
    	interfaces.add(DynaBean.class);
    	interfaces.add(Stoppable.class);
    	interfaces.add(Iconic.class);
    	interfaces.add(LogEnabled.class);
    	if (!(wrapped instanceof Serializable)) {
    		interfaces.add(Transient.class);
    	}
    	
    	Class<?>[] interfaceArray = 
    		(Class[]) interfaces.toArray(new Class[0]);
    	
    	wrapper.proxy = 
    		Proxy.newProxyInstance(classLoader,
    			interfaceArray,
    			wrapper);
    	
    	return (Runnable) wrapper.proxy;
    }

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object destination = methods.get(method);
		if (destination == null) {
			// hashCode etc
			destination = this;
		}
		return method.invoke(destination, args);
	}

	public Object getWrapped() {
		return wrapped;
	}

	protected DynaBean getDynaBean() {
		return dynaBean;
	}

	protected Object getProxy() {
		return proxy;
	}

	protected void setWrapped(Runnable wrapped) {
		this.wrapped = wrapped;
		this.dynaBean = new WrapDynaBean(wrapped);
		this.methods = new HashMap<Method, Object>();

		Class<?>[] interfaces = interfacesFor(wrapped);
		for (int i = 0; i < interfaces.length; ++i) {
			addMethods(interfaces[i], wrapped);
		}

		addMethods(ArooaContextAware.class, this);
		addMethods(Stateful.class, this);
		addMethods(Resetable.class, this);
		addMethods(DynaBean.class, this);
		addMethods(Stoppable.class, this);
		addMethods(Iconic.class, this);
		addMethods(Runnable.class, this);
		addMethods(LogEnabled.class, this);
	}

	/**
	 * Add a method and the object that is going to implement it.
	 * 
	 * @param from
	 * @param destination
	 */
	private void addMethods(Class<?> from, Object destination) {
		Method[] ms = from.getDeclaredMethods();
		for (int i = 0; i < ms.length; ++i) {
			methods.put(ms[i], destination);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		OddjobNDC.push(loggerName());
		try {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					getStateChanger().setJobState(JobState.EXECUTING);
				}	
			})) {
				return;			
			}
			
			logger().info("[" + wrapped + "] Executing.");
			
			final Throwable[] exception = new Throwable[1];
			
			thread = Thread.currentThread();
			try {
				configure();
				wrapped.run();
			} catch (Throwable t) {
				logger().error("[" + wrapped + "] Exception:", t);
				exception[0] = t;
			} finally {
				thread = null;
			}

			logger().info("[" + wrapped + "] Finished.");
					
			stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
	
					if (exception[0] != null) {
						getStateChanger().setJobStateException(exception[0]);
					}
					else {
						int result = getResult();
						if (result == 0) {
							getStateChanger().setJobState(JobState.COMPLETE);
						} else {
							getStateChanger().setJobState(JobState.INCOMPLETE);
						}
					}
				}
			});
		} finally {
			thread = null;
			OddjobNDC.pop();
		}
	}

	public void onStop() throws FailedToStopException {
		if (wrapped instanceof Stoppable) {
			((Stoppable) wrapped).stop();
		} else {
			Thread t = thread;
			if (t != null){
				thread.interrupt();
			}
		}
	}

	/**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		s.writeObject(stateHandler.lastJobStateEvent());
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) throws IOException,
			ClassNotFoundException {
		s.defaultReadObject();
		setWrapped(wrapped);
		JobStateEvent savedEvent = (JobStateEvent) s.readObject();
		stateHandler.restoreLastJobStateEvent(savedEvent);
		iconHelper.changeIcon(StateIcons.iconFor(stateHandler.getJobState()));
		completeConstruction();
	}

}
