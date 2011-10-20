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
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.FailedToStopException;
import org.oddjob.Iconic;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.images.StateIcons;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.persist.Persistable;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateChanger;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.StateEvent;

/**
 * Creates a proxy for any {@link java.lang.Runnable} to allow it to be controlled and
 * monitored within Oddjob.
 * 
 * @author Rob Gordon.
 */
public class RunnableWrapper extends BaseWrapper implements InvocationHandler,
		Serializable {
	private static final long serialVersionUID = 20051231;

	private transient JobStateHandler stateHandler;
	
	private transient JobStateChanger stateChanger;
	
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
		stateHandler = new JobStateHandler(this);
		stateChanger = new JobStateChanger(stateHandler, iconHelper, 
				new Persistable() {					
					@Override
					public void persist() throws ComponentPersistException {
						save();
					}
				});
	}
	
	@Override
	protected JobStateHandler stateHandler() {
		return stateHandler;
	}
	
	protected JobStateChanger getStateChanger() {
		return stateChanger;
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
		
		OddjobNDC.push(loggerName(), this);
		try {
			thread = Thread.currentThread();
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					getStateChanger().setState(JobState.EXECUTING);
				}	
			})) {
				return;			
			}
			
			logger().info("Executing.");
			
			final AtomicReference<Throwable> exception = 
				new AtomicReference<Throwable>();;
			
			try {
				configure();
				wrapped.run();
			} catch (Throwable t) {
				logger().error("Exception:", t);
				exception.set(t);
			} finally {
				thread = null;
			}
			
			logger().info("Finished.");
					
			stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
	
					if (exception.get() != null) {
						getStateChanger().setStateException(exception.get());
					}
					else {
						int result = getResult();
						if (result == 0) {
							getStateChanger().setState(JobState.COMPLETE);
						} else {
							getStateChanger().setState(JobState.INCOMPLETE);
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
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
			public void run() {
				getStateChanger().setState(JobState.READY);
				
				logger().info("Soft Reset complete.");
			}
		});
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		
		return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
			public void run() {
				getStateChanger().setState(JobState.READY);

				logger().info("Hard Reset complete.");
			}
		});
	}
	
	/**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		s.writeObject(stateHandler.lastStateEvent());
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) throws IOException,
			ClassNotFoundException {
		s.defaultReadObject();
		setWrapped(wrapped);
		StateEvent savedEvent = (StateEvent) s.readObject();
		completeConstruction();
		stateHandler.restoreLastJobStateEvent(savedEvent);
		iconHelper.changeIcon(StateIcons.iconFor(stateHandler.getState()));
	}

	/**
	 * Internal method to fire state.
	 */
	protected void fireDestroyedState() {
		
		if (!stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				stateHandler().setState(JobState.DESTROYED);
				stateHandler().fireEvent();
			}
		})) {
			throw new IllegalStateException("[" + RunnableWrapper.this + "] Failed set state DESTROYED");
		}
		
		logger().debug("[" + this + "] Destroyed.");				
	}
}
