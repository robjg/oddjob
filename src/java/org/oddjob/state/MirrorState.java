package org.oddjob.state;

import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.BasePrimary;
import org.oddjob.framework.JobDestroyedException;


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

	private Stateful job;
	
	private JobStateListener listener;
	
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
				
		stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
			public void run() {
				if (listener != null) {
					// still mirroring.
					return;
				}
				
				logger().info("[" + MirrorState.this + "] Starting.");
				
				try {
					configure();
				} catch (ArooaConfigurationException e) {
					getStateChanger().setJobStateException(
							e);
					logger().error("[" + MirrorState.this + 
							"] Exception configuring.", e);
					return;
				}

				if (job == null) {
					getStateChanger().setJobStateException(
							new NullPointerException("No Job."));
					return;
				}

				logger().info("Starting to mirror [" + job + "]");

				listener = new MirrorListener();
					
				job.addJobStateListener(listener);
			}
		});
	}

	class MirrorListener implements JobStateListener {

		public synchronized void jobStateChange(
				final JobStateEvent event) {
			logger().info("Mirroring [" + event.getJobState() + "], time [" +
					event.getTime() + "]");
			
			stateHandler.waitToWhen(new IsAnyState(), 
					new Runnable() {
				public void run() {
					JobState state = event.getJobState();
					
					if (state == JobState.DESTROYED) {
						logger().info("Target Destroyed! Raising Exception.");
						
						getStateChanger().setJobStateException(
								new JobDestroyedException(job));
						
						stop();
					}
					else {
						if (state == JobState.EXCEPTION){
							
							getStateChanger().setJobStateException(
									event.getException(), event.getTime());
						}
						else {
						
							getStateChanger().setJobState(state, event.getTime());
						}
						
					}
				}
			});
		}
	}
	
	public synchronized void stop() {
		if (listener != null) {
			job.removeJobStateListener(listener);
			listener = null;
			logger().info("Stopped mirroring [" + job + "]");
			stateHandler.waitToWhen(new IsStoppable(), 
					new Runnable() {
				public void run() {
					getStateChanger().setJobState(JobState.READY);
				}
			});
			job = null;
		}
	}
	
	private synchronized boolean reset() {
		stop();
		return stateHandler.waitToWhen(new StateCondition() {			
			@Override
			public boolean test(JobState state) {
				return JobState.READY != state;
			}
		}, 
				new Runnable() {
			public void run() {
				getStateChanger().setJobState(JobState.READY);
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
}
