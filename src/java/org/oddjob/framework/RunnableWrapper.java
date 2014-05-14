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
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.images.IconHelper;
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
	private static final long serialVersionUID = 20012052320051231L;

	/** Handle state. */
	private transient volatile JobStateHandler stateHandler;
	
	/** Used to notify clients of an icon change. */
	private transient volatile IconHelper iconHelper;
	
	/** Perform the state change. */
	private transient volatile JobStateChanger stateChanger;
	
	/** The wrapped Runnable. */
	private volatile Object wrapped;
	
	/**
	 * The DynaBean that takes its properties of the wrapped Runnable.
	 */
	private transient volatile DynaBean dynaBean;

	/** The thread our job is executing on. */
	private transient volatile Thread thread;

	/**
	 * The proxy we create that represents our wrapped Runnable within Oddjob.
	 */
	private final Object proxy;

	/** Reset with annotations adaptor. */
	private transient volatile Resetable resetableAdaptor;
	
	/**
	 * Constructor.
	 * 
	 */
	public RunnableWrapper(Object wrapped, Object proxy) {
		this.wrapped = wrapped;
		this.proxy = proxy;
		completeConstruction();
	}

	/**
	 * Complete construction. Called by constructor and post
	 * deserialisation.
	 */
	private void completeConstruction() {
		this.dynaBean = new WrapDynaBean(wrapped);
		stateHandler = new JobStateHandler((Stateful) proxy);
		iconHelper = new IconHelper(this, 
				StateIcons.iconFor(stateHandler.getState()));
		stateChanger = new JobStateChanger(stateHandler, iconHelper, 
				new Persistable() {					
					@Override
					public void persist() throws ComponentPersistException {
						save();
					}
				});
	}
	
	@Override
	public void setArooaSession(ArooaSession session) {
		super.setArooaSession(session);
		resetableAdaptor = new ResetableAdaptorFactory().resetableFor(
				wrapped, session);
	}
	
	@Override
	protected IconHelper iconHelper() {
		return iconHelper;
	}
	
	@Override
	protected JobStateHandler stateHandler() {
		return stateHandler;
	}
	
	protected JobStateChanger getStateChanger() {
		return stateChanger;
	}
	
	@Override
	protected Object getWrapped() {
		return wrapped;
	}

	@Override
	protected DynaBean getDynaBean() {
		return dynaBean;
	}

	@Override
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
			
			thread = Thread.currentThread();
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
				stateHandler.callLocked(new Callable<Void>() {
					@Override
					public Void call() {
						if (Thread.interrupted()) {
							logger().debug("Clearing thread interrupted flag.");
						}
						thread = null;
						return null;
					}
				});
				
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
			ComponentBoundry.pop();
		}
	}

	@Override
	public void onStop() throws FailedToStopException {
		if (wrapped instanceof Stoppable) {
			((Stoppable) wrapped).stop();
		} else {
			stateHandler.callLocked(new Callable<Void>() {
				@Override
				public Void call() {
					Thread t = thread;
					if (t != null){
						logger().info("Interrupting Thread [" + t.getName() +
								"] to attempt to stop job.");
						t.interrupt();
					}
					else {
						logger().info("No Thread to interrupt. Hopefully Job has just stopped.");
					}
					return null;
				}
			});
		}
	}

	/**
	 * Perform a soft reset on the job.
	 */
	@Override
	public boolean softReset() {
		ComponentBoundry.push(loggerName(), this);
		try {
			return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {
					if (resetableAdaptor == null) {
						throw new NullPointerException(
							"ResetableAdaptor hasn't been set, " +
							"setArooaSession() must be called on the proxy.");
					}
					
					resetableAdaptor.softReset();
					
					getStateChanger().setState(JobState.READY);
					
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
	@Override
	public boolean hardReset() {
		ComponentBoundry.push(loggerName(), this);
		try {
			return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
				public void run() {
					if (resetableAdaptor == null) {
						throw new NullPointerException(
							"ResetableAdaptor hasn't been set, " +
							"setArooaSession() must be called on the proxy.");
					}
					
					resetableAdaptor.hardReset();
					
					getStateChanger().setState(JobState.READY);
						
					logger().info("Hard Reset complete.");
				}
			});
		}
		finally {
			ComponentBoundry.pop();
		}
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
		s.writeObject(stateHandler.lastStateEvent().serializable());
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) throws IOException,
			ClassNotFoundException {
		s.defaultReadObject();
		StateEvent.SerializableNoSource savedEvent = 
				(StateEvent.SerializableNoSource) s.readObject();
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
