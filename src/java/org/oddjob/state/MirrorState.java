package org.oddjob.state;

import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.framework.extend.BasePrimary;
import org.oddjob.framework.util.ComponentBoundry;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.persist.Persistable;
import org.oddjob.util.Restore;


/**
 * @oddjob.description
 * 
 * When run this job mirrors the state of the given job. It continues
 * to do so until it's stopped.
 * 
 * @author Rob Gordon
 */
public class MirrorState extends BasePrimary
implements Runnable, Stoppable, Resetable {

	/** Handle state. */
	private final JobStateHandler stateHandler;
	
	/** Used to notify clients of an icon change. */
	private final IconHelper iconHelper;
	
	/** Used to change state. */
	private final JobStateChanger stateChanger;
	
	private volatile Stateful job;
	
	private volatile StateListener listener;
	
	public MirrorState() {
		stateHandler = new JobStateHandler(this);
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
	protected JobStateHandler stateHandler() {
		return stateHandler;
	}
	
	@Override
	protected IconHelper iconHelper() {
		return iconHelper;
	}
	
	protected StateChanger<JobState> getStateChanger() {
		return stateChanger;
	}
	
	
    /**
     * @oddjob.property job
     * @oddjob.description A reference to the job to mirror.
     * @oddjob.required Yes.
     */
	@ArooaAttribute
	public synchronized void setJob(Stateful job) {
		this.job = job;
	}
	
	synchronized public void run() {
				
		try (Restore restore = ComponentBoundry.push(loggerName(), this)) {
			stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					if (listener != null) {
						// still mirroring.
						return;
					}

					logger().info("Starting.");

					try {
						configure();
					} catch (ArooaConfigurationException e) {
						getStateChanger().setStateException(
								e);
						logger().error("Exception configuring.", e);
						return;
					}

					if (job == null) {
						getStateChanger().setStateException(
								new NullPointerException("No Job."));
						return;
					}

					logger().info("Starting to mirror [" + job + "]");

					listener = new MirrorListener();

					job.addStateListener(listener);
				}
			});
		}
	}

	class MirrorListener implements StateListener {

		@Override
		public synchronized void jobStateChange(
				final StateEvent event) {
			logger().info("Mirroring [" + event.getState() + "], time [" +
					event.getTime() + "]");
			
			stateHandler.waitToWhen(new IsAnyState(), 
					new Runnable() {
				public void run() {
					State state = event.getState();
					
					if (state.isDestroyed()) {
						logger().info("Target Destroyed! Raising Exception.");
						
						getStateChanger().setStateException(
								new JobDestroyedException(job));
						
						stop();
					}
					else {
						if (state.isException()){
							
							getStateChanger().setStateException(
									event.getException(), event.getTime());
						}
						else {
						
							getStateChanger().setState(
									new JobStateConverter().toJobState(
											state), event.getTime());
						}
						
					}
				}
			});
		}
	}
	
	public synchronized void stop() {
		try (Restore restore = ComponentBoundry.push(loggerName(), this)) {
			if (listener != null) {
				job.removeStateListener(listener);
				listener = null;
				logger().info("Stopped mirroring [" + job + "]");
				stateHandler.waitToWhen(new IsStoppable(), 
						new Runnable() {
					public void run() {
						getStateChanger().setState(JobState.READY);
					}
				});
				job = null;
			}
		}
	}
	
	private synchronized boolean reset() {
		stop();
		return stateHandler.waitToWhen(new StateCondition() {			
			@Override
			public boolean test(State state) {
				return JobState.READY != state;
			}
		}, 
				new Runnable() {
			public void run() {
				getStateChanger().setState(JobState.READY);
			}
		});
	}
	
	public boolean hardReset() {
		return reset();
	}
	
	public boolean softReset() {
		return reset();
	}
	
	@Override
	public void onDestroy() {
		stop();
		super.onDestroy();
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
			throw new IllegalStateException("[" + MirrorState.this + "] Failed to set state DESTROYED");
		}
		logger().debug("[" + this + "] Destroyed.");				
	}
}
