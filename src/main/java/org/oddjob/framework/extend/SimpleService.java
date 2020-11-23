package org.oddjob.framework.extend;

import org.oddjob.FailedToStopException;
import org.oddjob.Resettable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.logging.LogEnabled;
import org.oddjob.persist.Persistable;
import org.oddjob.state.*;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for providing a common Service implementation.
 * <p>
 * Note that this class is only required when special interaction with
 * Oddjob is required such as different state handling. Most simple
 * service can be provided a classes to be proxied.
 * 
 * @author rob
 *
 */
abstract public class SimpleService extends BaseComponent 
implements Runnable, Stateful, Resettable,
		Stoppable, LogEnabled {

	private static final AtomicInteger instanceCount = new AtomicInteger();
	
	/** Instance of the logger. */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName() + 
			"." + instanceCount.incrementAndGet());
	
	/** Handle state. */
	private final ServiceStateHandler stateHandler; 
	
	/** Used to notify clients of an icon change. */
	private final IconHelper iconHelper;
	
	/** Used to change state. */
	private final ServiceStateChanger stateChanger;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private volatile String name;
		
	/**
	 * Constructor.
	 * 
	 */
	public SimpleService() {
		stateHandler = new ServiceStateHandler(this);
		iconHelper = new IconHelper(this, 
				StateIcons.iconFor(stateHandler.getState()));
		stateChanger = new ServiceStateChanger(stateHandler, iconHelper, 
				new Persistable() {					
			@Override
			public void persist() throws ComponentPersistException {
				save();
			}
		});
	}
	
	@Override
	protected Logger logger() {
		return logger;
	}
	
	@Override
	public String loggerName() {
		return logger.getName();
	}
	
	@Override
	protected ServiceStateHandler stateHandler() {
		return stateHandler;
	}
	
	@Override
	protected IconHelper iconHelper() {
		return iconHelper;
	}
	
	protected ServiceStateChanger getStateChanger() {
		return stateChanger;
	}
    
	
	public void run() {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					getStateChanger().setState(ServiceState.STARTING);
					
				}
			})) {
				return;
			}
			logger().info("Starting.");
			
			try {
				configure(SimpleService.this);
				
				onStart();
				
            	stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
            		public void run() {
            			getStateChanger().setState(ServiceState.STARTED);
            		}   
            	});				
			}
			catch (final Throwable e) {
				logger().warn("Exception starting:", e);
				
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						getStateChanger().setStateException(e);
					}
				});
			}
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	abstract protected void onStart() throws Throwable;
	
	@Override
	public void stop() throws FailedToStopException {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			logger().debug("Stop requested.");
			
			if (!stateHandler.waitToWhen(new IsStoppable(), 
					new Runnable() {
				public void run() {
					iconHelper.changeIcon(IconHelper.STOPPING);
				}
			})) {
				logger().debug("Stop ignored - not running.");
				return;
			}
	
			logger().info("Stopping.");
			
			try {
				onStop();
				
				logger().info("Stopped.");
				
	        	stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
	        		public void run() {
	        			getStateChanger().setState(ServiceState.STOPPED);
	        		}   
	        	});				
			} catch (final Exception e) {
				logger().warn("Exception starting:", e);
				
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						getStateChanger().setStateException(e);
					}
				});
			}
		}
	}
	
	/**
	 * Allow sub classes to do something on stop.
	 */
	protected void onStop() throws FailedToStopException { }
	
	/**
	 * Perform a soft reset on the job.
	 */
	@Override
	public boolean softReset() {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {
					getStateChanger().setState(ServiceState.STARTABLE);
	
					logger().info("Soft Reset complete." );
				}
			});
		} 
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	@Override
	public boolean hardReset() {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
				public void run() {
					getStateChanger().setState(ServiceState.STARTABLE);
	
					logger().info("Hard Reset complete." );
				}
			});
		} 
	}

	
	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name
	 * 
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	

	@Override
	public String toString() {
	    if (name == null) {
	        return getClass().getSimpleName();
	    }
	    else {
	        return name;
	    }
	}

	@Override
	protected void onDestroy() {		
		super.onDestroy();
		
		try {
			stop();
		} catch (FailedToStopException e) {
			logger().warn("Failed to stop.", e);
		}
	}
	
	/**
	 * Internal method to fire state.
	 */
	@Override
	protected void fireDestroyedState() {
		
		if (!stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				stateHandler().setState(ServiceState.DESTROYED);
				stateHandler().fireEvent();
			}
		})) {
			throw new IllegalStateException("[" + SimpleService.this + 
					"[ Failed set state DESTROYED");
		}
		logger().debug("[" + this + "] Destroyed.");				
	}

}
