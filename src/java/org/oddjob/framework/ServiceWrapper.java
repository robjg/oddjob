/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.framework;

import java.beans.ExceptionListener;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.FailedToStopException;
import org.oddjob.arooa.life.ComponentPersistException;
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
 * Wraps a Service object and adds state to it. 
 * <p>
 * 
 * @author Rob Gordon.
 */
public class ServiceWrapper extends BaseWrapper
implements ComponentWrapper {
	
	private final ServiceStateHandler stateHandler;
	
	private final ServiceStateChanger stateChanger;
	
    private final ServiceAdaptor service;
    
    private Object wrapped;
    
    private transient DynaBean dynaBean;
    
    private final Object proxy;
    	
    public ServiceWrapper(ServiceAdaptor service, Object proxy) {
    	this.service = service;
    	this.proxy = proxy;
        this.wrapped = service.getComponent();
        this.dynaBean = new WrapDynaBean(wrapped);    	
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
    
    public Object getWrapped() {
        return wrapped;
    }

    protected DynaBean getDynaBean() {
        return dynaBean;
    }

    protected Object getProxy() {
        return proxy;
    }

    @Override
    protected void save(Object compoonent) {
    	// Services don't persist. Maybe they should.
    }
    
    public void run() {
		ComponentBoundry.push(loggerName(), wrapped);
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
        		if (Thread.interrupted()) {
        			throw new InterruptedException(
        					"Service Thread interrupt flag set. " +
        					"Please ensure previous jobs clean up after " +
        					"themselves to guarantee consistent behaviour.");
        		}
        		
        		service.acceptExceptionListener(new ExceptionListener() {
					@Override
					public void exceptionThrown(Exception e) {
						exceptionFromComponent(e);
					}
				});
				
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
    	    	logger().error("Exception starting service:", t);
    			
    	    	stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
    	    		public void run() {
        				getStateChanger().setStateException(t);
    	    		}
    	    	});
    	    }
        }
        finally {
        	ComponentBoundry.pop();
        }
    }
    
    /**
     * Used by the exception handler callback.
     * 
     * @param e
     */
    protected void exceptionFromComponent(final Exception e) {
    	logger().error("Exception starting service:", e);
		
    	if (!stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
    		public void run() {
				getStateChanger().setStateException(e);
    		}
    	})) {
    		throw new IllegalStateException("Service has not started.");
    	}
    }
        
    public void onStop() throws FailedToStopException {
    	stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
    		public void run() {
    		}
    	});
    			
		ComponentBoundry.push(loggerName(), wrapped);
		try {
			service.stop();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new FailedToStopException(service, e);
		}
        finally {
        	ComponentBoundry.pop();
        }
        
    	stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
    		public void run() {
    			getStateChanger().setState(ServiceState.COMPLETE);
    		}
    	});

    }
    
	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		ComponentBoundry.push(loggerName(), wrapped);
        try {
			return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {
					getStateChanger().setState(ServiceState.READY);
					
					logger().info("Soft Reset complete.");
				}
			});
        }
        finally {
        	ComponentBoundry.pop();
        }
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		
		ComponentBoundry.push(loggerName(), wrapped);
        try {
        	return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
				public void run() {
					getStateChanger().setState(ServiceState.READY);
	
					logger().info("Hard Reset complete.");
				}
			});
        }
        finally {
        	ComponentBoundry.pop();
        }
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
