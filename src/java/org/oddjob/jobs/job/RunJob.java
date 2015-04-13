package org.oddjob.jobs.job;

import java.util.LinkedList;

import org.oddjob.OddjobComponentResolver;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.parsing.OwnerStateListener;
import org.oddjob.arooa.parsing.SerializableDesignFactory;
import org.oddjob.framework.ComponentBoundry;
import org.oddjob.framework.SimpleJob;
import org.oddjob.framework.StructuralJob;
import org.oddjob.images.IconHelper;
import org.oddjob.state.AnyActiveStateOp;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.ParentState;
import org.oddjob.state.State;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.state.StateOperator;
import org.oddjob.util.OddjobConfigException;

/**
 * @oddjob.description A job which runs another job. The other job can be
 * local or on a server.
 * <p>
 * This job reflects the state of the job being executed.
 * <p>
 * TODO: Why does this job implement {@link ConfigurationOwner}????
 * 
 * @oddjob.example
 * 
 * Examples elsewhere.
 * <ul>
 *  <li>The {@link org.oddjob.jmx.JMXClientJob} job has an
 *  example that uses <code>run</code> to run a job on a 
 *  remote server.</li>
 * </ul>
 *  
 * 
 * @author Rob Gordon
 */

public class RunJob extends StructuralJob<Object>
implements Structural, Stoppable, ConfigurationOwner {
    private static final long serialVersionUID = 20050806201204300L;

	/** 
	 * @oddjob.property
	 * @oddjob.description Job to run
	 * @oddjob.required Yes.
	 */
	private volatile transient Object job;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The reset level. See {@link ResetActions}. 
	 * @oddjob.required No, defaults to NONE.
	 */
	private volatile transient ResetAction reset;
	
	/**
	 * Set the stop node directly.
	 * 
	 * @param node The job.
	 */
	@ArooaAttribute
	synchronized public void setJob(Object node) {
		this.job = node;
	}

	/**
	 * Get the job.
	 * 
	 * @return The node.
	 */
	synchronized public Object getJob() {
		return this.job;
	}	
	
	@Override
	protected StateOperator getInitialStateOp() {
		return new StateOperator() {
			@Override
			public ParentState evaluate(State... states) {
				// Our state operator must cope with a client node
				// that has been destroyed because the client has
				// been stopped.
				if (states.length > 0 && states[0].isDestroyed()) {
					if (childStateReflector.isRunning()) { 
						ComponentBoundry.push(loggerName(), RunJob.this);
						try {
							childStateReflector.stop();
							childHelper.removeAllChildren();

							stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
								@Override
								public void run() {
									if (stateHandler().lastStateEvent().getState().isStoppable()) {
										logger().info("Job Destroyed while active, setting state to COMPLETE");
										getStateChanger().setState(ParentState.COMPLETE);
									}
									else {
										logger().info("Job Destroyed, leaving job in previous state.");
									}
								}
							});
						}
						finally {
							ComponentBoundry.pop();
						}
					}
					// this will not be used.
					return ParentState.READY;
				}
				else {
					return new AnyActiveStateOp().evaluate(states);
				}
			}
		};
	}

	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected void execute() throws Exception {
		
		if (job == null) {
			throw new OddjobConfigException("A job to start must be provided.");
		}
		
		Object proxy;
		if (childHelper.size() == 0) {
			OddjobComponentResolver resolver = new OddjobComponentResolver();
			
			proxy = resolver.resolve(job, getArooaSession());
			
			// if we created the proxy we need to set the session.
			// This is required for reset actions that might use descriptor 
			// annotations. We don't set context and so don't have any
			// way to destroy the proxy. This might need to be addressed
			// at a later date.
			if (proxy != job && proxy instanceof ArooaSessionAware) {
				((ArooaSessionAware) proxy).setArooaSession(
						getArooaSession());
			}
			
			childHelper.addChild(proxy);
		}
		else {
			proxy = childHelper.getChild();
		}
		
		ResetAction reset = this.reset;
		if (reset == null) {
			reset = ResetActions.NONE;
		}
		reset.doWith(job);		
			
		final LinkedList<State> states = new LinkedList<State>();
		StateListener listener = null;
		
		if (job instanceof Stateful) {
			listener = new StateListener() {
				public void jobStateChange(StateEvent event) {
					synchronized (states) {
						states.add(event.getState());
						states.notifyAll();
					}
				}
			};
			
			((Stateful) job).addStateListener(listener);
		}
		
		if (proxy instanceof Runnable) {
			Runnable runnable = (Runnable) proxy;
			runnable.run();
		}
		
		if (job instanceof Stateful) {
			
			boolean executed = false;
			try {
				while (!stop) {
	
					State now = null;
					
					synchronized (states) {
						if (!states.isEmpty()) {
							now = states.removeFirst();
							logger().debug("State received "+ now);
						}
						else {
							logger().debug("Waiting for job to finish executing");
							iconHelper().changeIcon(IconHelper.SLEEPING);
							try {
								states.wait(0);
							} catch (InterruptedException e) {
								logger().debug("Sleep interupted.");
								Thread.currentThread().interrupt();
							}
							// Stop should already have set Icon to Stopping.
							if (!stop) {
								iconHelper().changeIcon(IconHelper.EXECUTING);
							}
						}
					}
					
					if (now != null) {						
						if (now.isDestroyed()) {
							childHelper.removeAllChildren();
							throw new IllegalStateException("Job Destroyed.");
						}
						
						if (now.isStoppable()) {
							executed = true;
						}
						
						// when the thread of control has moved passed a job.						
						if (now.isComplete() && 
								(executed || !now.isReady())) {
							logger().debug("Job has executed. State is " + now);
							break;
						}
						continue;
					}
				}
			}
			finally {
				((Stateful) job).removeStateListener(listener);		
			}
		}
	}

	/**
	 * Sleep. This is a copy of {@link SimpleJob#sleep}.
	 * 
	 * @param waitTime Time in milliseconds to sleep for.
	 */
	protected void sleep(final long waitTime) {
		stateHandler().assertAlive();
		
		if (!stateHandler().waitToWhen(new IsStoppable(), new Runnable() {
			public void run() {
				if (stop) {
					logger().debug("Stop request detected. Not sleeping.");
					
					return;
				}
				
				logger().debug("Sleeping for " + ( 
						waitTime == 0 ? "ever" : "[" + waitTime + "] milli seconds") + ".");
				
				iconHelper().changeIcon(IconHelper.SLEEPING);
					
				try {
					stateHandler().sleep(waitTime);
				} catch (InterruptedException e) {
					logger().debug("Sleep interupted.");
					Thread.currentThread().interrupt();
				}
				
				// Stop should already have set Icon to Stopping.
				if (!stop) {
					iconHelper().changeIcon(IconHelper.EXECUTING);
				}
			}
		})) {
			throw new IllegalStateException("Can't sleep unless EXECUTING.");
		}
	}
	

	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		ComponentBoundry.push(loggerName(), this);
		try {
			return stateHandler().waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {
				
					logger().debug("Propagating Soft Reset to children.");			
					
					childStateReflector.stop();
					childHelper.removeAllChildren();
					stop = false;
					getStateChanger().setState(ParentState.READY);
					
					logger().info("Soft Reset complete.");
				}
			});	
		} finally {
			ComponentBoundry.pop();
		}
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		
		ComponentBoundry.push(loggerName(), this);
		try {
			return stateHandler().waitToWhen(new IsHardResetable(), new Runnable() {
				public void run() {
					logger().debug("Propagating Hard Reset to children.");			
					
					childStateReflector.stop();
					childHelper.removeAllChildren();
					stop = false;
					getStateChanger().setState(ParentState.READY);
					
					logger().info("Hard Reset complete.");
				}
			});
		} finally {
			ComponentBoundry.pop();
		}
	}
	
	
	@Override
	public void addOwnerStateListener(OwnerStateListener listener) {
	}
	@Override
	public void removeOwnerStateListener(OwnerStateListener listener) {
	}
	@Override
	public ConfigurationSession provideConfigurationSession() {
		return null;
	}
	@Override
	public SerializableDesignFactory rootDesignFactory() {
		return null;
	}
	@Override
	public ArooaElement rootElement() {
		return null;
	}

	public ResetAction getReset() {
		return reset;
	}

	@ArooaAttribute
	public void setReset(ResetAction reset) {
		this.reset = reset;
	}
}
