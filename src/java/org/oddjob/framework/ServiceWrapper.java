/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.framework;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.FailedToStopException;
import org.oddjob.Iconic;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.persist.Persistable;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.ServiceState;
import org.oddjob.state.ServiceStateChanger;
import org.oddjob.state.ServiceStateHandler;

/**
 * Wraps a Runnable object and adds state to it. 
 * <p>
 * This is a helper class for parent jobs which depend on their
 * child being Stateful - this pretends an un Stateful Runnable
 * is Stateful thus allowing the parent to accept plain Runnables
 * as children.
 * 
 * @author Rob Gordon.
 */
public class ServiceWrapper extends BaseWrapper
implements InvocationHandler {
	
	private final ServiceStateHandler stateHandler;
	
	private final ServiceStateChanger stateChanger;
	
    private Service service;
    
    private Object wrapped;
    private transient DynaBean dynaBean;
    
    private transient Map<Method, Object> methods;
    
    private Object proxy;
    	
    private ServiceWrapper(Service service) {
    	setWrapped(service);
    	stateHandler = new ServiceStateHandler(this);
    	stateChanger = new ServiceStateChanger(stateHandler, iconHelper, 
    			new Persistable() {					
    		@Override
    		public void persist() throws ComponentPersistException {
    			save();
    		}
    	});
    }
    
    @Override
    protected ServiceStateHandler stateHandler() {
    	return stateHandler;
    }
    
	protected ServiceStateChanger getStateChanger() {
		return stateChanger;
	}
    
    public static Runnable wrapperFor(Service 
    		service, ClassLoader classLoader) {
    	
    	ServiceWrapper wrapper = new ServiceWrapper(service);
    	
    	Set<Class<?>> interfaces = new HashSet<Class<?>>();
    	interfaces.addAll(Arrays.asList(interfacesFor(service.getComponent())));
    	interfaces.add(ArooaContextAware.class);
    	interfaces.add(Runnable.class);
    	interfaces.add(Stateful.class);
    	interfaces.add(Resetable.class);
    	interfaces.add(DynaBean.class);
    	interfaces.add(Stoppable.class);
    	interfaces.add(Iconic.class);
    	interfaces.add(LogEnabled.class);
    	if (!(service instanceof Serializable)) {
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
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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

    protected void setWrapped(Service service) {
    	this.service = service;
        this.wrapped = service.getComponent();
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
    
    @Override
    protected void save(Object compoonent) {
    	// Services don't persist. Maybe they should.
    }
    
    private void addMethods(Class<?> from, Object destination) {
    	Method[] ms = from.getDeclaredMethods();
    	for (int i = 0; i < ms.length; ++i) {
    		methods.put(ms[i], destination);
    	}
    }
    
    public void run() {
		OddjobNDC.push(loggerName(), this);
        try {
        	if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
        		public void run() {
        			getStateChanger().setState(ServiceState.STARTING);
        		}   
        	})) {
        		return;
        	}
        	
			logger().info("Starting.");
			
        	try {
        		configure();
        		
        		service.start();

    	        logger().info("Started.");
    	        
            	stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
            		public void run() {
            			getStateChanger().setState(ServiceState.STARTED);
            		}   
            	});
    	    } 
        	catch (final Throwable t) {
    	    	logger().error("Exception:", t);
    			
    	    	stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
    	    		public void run() {
        				getStateChanger().setStateException(t);
    	    		}
    	    	});
    	    }
        }
        finally {
        	OddjobNDC.pop();
        }
    }
        
    public void onStop() throws FailedToStopException {
    	stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
    		public void run() {
    		}
    	});
    	
		final AtomicInteger result = new AtomicInteger();
		
		OddjobNDC.push(loggerName(), this);
		try {
			service.stop();
			result.set(getResult());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new FailedToStopException(service, e);
		}
        finally {
        	OddjobNDC.pop();
        }
        
    	stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
    		public void run() {
                if (result.get() == 0) {
                	getStateChanger().setState(ServiceState.COMPLETE);
                } else {
                	getStateChanger().setState(ServiceState.INCOMPLETE);
                }
    		}
    	});

    }
    
	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
			public void run() {
				getStateChanger().setState(ServiceState.READY);
				
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
				getStateChanger().setState(ServiceState.READY);

				logger().info("Hard Reset complete.");
			}
		});
	}
	
	/**
	 * Internal method to fire state.
	 */
	protected void fireDestroyedState() {
		
		if (!stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				stateHandler().setState(ServiceState.DESTROYED);
				stateHandler().fireEvent();
			}
		})) {
			throw new IllegalStateException("[" + ServiceWrapper.this + "] Failed set state DESTROYED");
		}
		logger().debug("[" + this + "] Destroyed.");				
	}
}
