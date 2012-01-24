/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.FailedToStopException;
import org.oddjob.Forceable;
import org.oddjob.Stoppable;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.images.StateIcons;
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
public class RunnableWrapper extends BaseWrapper 
implements ComponentWrapper, Serializable, Forceable {
	private static final long serialVersionUID = 20051231;

	private transient JobStateHandler stateHandler;
	
	private transient JobStateChanger stateChanger;
	
	/** The wrapped Runnable. */
	private Object wrapped;
	
	/**
	 * The DynaBean that takes its properties of the wrapped Runnable.
	 */
	private transient DynaBean dynaBean;

	/** The thread our job is executing on. */
	private volatile transient Thread thread;

	/**
	 * The proxy we create that represents our wrapped Runnable within Oddjob.
	 */
	private final Object proxy;

	/**
	 * Constructor.
	 * 
	 */
	public RunnableWrapper(Object wrapped, Object proxy) {
		this.wrapped = wrapped;
		this.proxy = proxy;
		completeConstruction();
	}

	private void completeConstruction() {
		this.dynaBean = new WrapDynaBean(wrapped);
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
	
	public Object getWrapped() {
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
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		ComponentBoundry.push(loggerName(), wrapped);
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
				new AtomicReference<Throwable>();
			final AtomicReference<Object> callableResult = 
				new AtomicReference<Object>();	
			try {
				configure();
				
				Object result;
				
				if (wrapped instanceof Callable<?>) {
					result = ((Callable<?>) wrapped).call();
				}
				else {
					((Runnable) wrapped).run();
					result = null;
				}
				callableResult.set(result);
				
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
						int result;
						try {
							result = getResult(callableResult.get());
							
							if (result == 0) {
								getStateChanger().setState(JobState.COMPLETE);
							} else {
								getStateChanger().setState(JobState.INCOMPLETE);
							}
						} catch (Exception e) {
							getStateChanger().setStateException(e);
						}
					}
				}
			});
		} finally {
			thread = null;
			ComponentBoundry.pop();
		}
	}

	@Override
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
	@Override
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
	@Override
	public boolean hardReset() {
		
		return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
			public void run() {
				getStateChanger().setState(JobState.READY);

				logger().info("Hard Reset complete.");
			}
		});
	}
	
	/**
	 * Force the job to COMPLETE.
	 */
	@Override
	public void force() {
		
		stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
			public void run() {
				logger().info("Forcing complete.");			
				
				getStateChanger().setState(JobState.COMPLETE);
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
