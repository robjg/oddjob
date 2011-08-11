package org.oddjob.persist;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.oddjob.FailedToStopException;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.framework.BasePrimary;
import org.oddjob.framework.StopWait;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsDone;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateChanger;
import org.oddjob.state.JobStateConverter;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.StateListener;
import org.oddjob.state.StateChanger;
import org.oddjob.state.StateEvent;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * @oddjob.description A Job that is capable of taking a snapshot of the
 * state of it's child jobs. An {@link org.oddjob.persist.ArchiveBrowserJob}
 * can be used to browse an archive created with this job.
 * 
 * @oddjob.example
 * 
 * Create an archive after each scheduled run. The time of the schedule
 * is used to identify the archive.
 * 
 * {@oddjob.xml.resource org/oddjob/persist/ArchiveJobTest.xml}
 * 
 * @author rob
 *
 */
public class ArchiveJob extends BasePrimary
implements 
	Runnable, Serializable, 
	Stoppable, Resetable, Stateful, Structural {

	private transient JobStateHandler stateHandler;
	
	private transient JobStateChanger stateChanger;
	
	private static final long serialVersionUID = 2010032500L;
	
	/** Track changes to children an notify listeners. */
	private transient ChildHelper<Runnable> childHelper; 
			
	/**
	 * @oddjob.property 
	 * @oddjob.description The identifier of the snapshot that will
	 * be taken when this job runs.
	 * @oddjob.required Yes.
	 */
	private Object archiveIdentifier;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description The name of the acrhive that all snapshots
	 * will be stored in.
	 * @oddjob.required Yes.
	 */
	private String archiveName;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description The persister to use to store archives.
	 * @oddjob.required Yes, but will fall back on the current Oddjob persister.
	 */
	private transient OddjobPersister archiver;
	
	
	private volatile transient StateListener listener;
	
	protected transient volatile boolean stop;
	
	/**
	 * Constructor.
	 */
	public ArchiveJob() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		stateHandler = new JobStateHandler(this);
		childHelper = new ChildHelper<Runnable>(this);
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
	
	protected StateChanger<JobState> getStateChanger() {
		return stateChanger;
	}
	
	/**
	 * Implement the main execute method for a job. This surrounds the 
	 * doExecute method of the sub class and sets state for the job.
	 */
	public final void run() {
		OddjobNDC.push(loggerName());
		try {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					getStateChanger().setState(JobState.EXECUTING);
				}					
			})) {
				return;
			}
			
			logger().info("[" + ArchiveJob.this + "] Executing.");

			try {
				configure();
				
				execute();
			}
			catch (final Throwable e) {
				logger().error("[" + ArchiveJob.this + "] Job Exception.", e);
				
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						getStateChanger().setStateException(e);
					}
				});
			}	
			logger().info("[" + ArchiveJob.this + "] Execution finished.");
		}
		finally {
			OddjobNDC.pop();
		}
	}
	
	protected void execute() throws Throwable {
		
		OddjobPersister archiver = this.archiver;
	
		if (archiver == null) {
			ComponentPersister persister = 
				getArooaSession().getComponentPersister();
			
			if (persister != null && 
					persister instanceof OddjobPersister) {
				archiver = ((OddjobPersister) persister);
			}
		}

		if (archiver == null) {
			throw new NullPointerException("No Archiver.");
		}
		
		if (archiveIdentifier == null) {
			throw new NullPointerException("No ArchiveIdentifier.");
		}
		
		final ComponentPersister finalArchiver = 
			archiver.persisterFor(archiveName);
		
		if (finalArchiver == null) {
			throw new NullPointerException("No Persister for [" + 
					archiveName + "]");
		}
		
		final Runnable child = childHelper.getChild();
		
		if (child == null) {
			return;
		}
		
		if (! (child instanceof Stateful)) {
			throw new IllegalArgumentException("Child must be stateful to be archived.");
		}
		
		listener = new StateListener() {

			@Override
			public void jobStateChange(final StateEvent event) {
				
				if (stop) {
					if (!stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
						public void run() {
							if (event.getState().isException()) {
								getStateChanger().setStateException(
										event.getException());
							}
							else {
								getStateChanger().setState(new JobStateConverter().toJobState(
										event.getState()));
							}
						}
					})) {
						logger().info("[" + ArchiveJob.this + 
								"] Stopping and reflecting child state [" +
								event.getState() + "].");
					}
					// don't persist when stopping.
					return;
				}
				
				if (new IsDone().test(event.getState())) {
					
					if (!stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
						public void run() {
							logger().info("[" + ArchiveJob.this + 
									"] Archiving [" + event.getSource() + 
									"] as [" + archiveIdentifier + 
									"] because " + event.getState() + ".");
							
							try {
								persist(event.getSource());
								
								if (event.getState().isException()) {
									getStateChanger().setStateException(
											event.getException());
								}
								else {
									getStateChanger().setState(new JobStateConverter().toJobState(
											event.getState()));
								}
							}
							catch (ComponentPersistException e) {
								logger().error("Failed to persist.", e);
								getStateChanger().setStateException(e);
							}
							
						}
					})) {
						logger().info("[" + ArchiveJob.this + 
								"] Not archiving as no longer executing.");
					}
				}
				else if (event.getState().isDestroyed()) {
					stopListening(event.getSource());
				}
			}
			
			private void persist(Stateful source) throws ComponentPersistException {
				OddjobNDC.push(loggerName());
				try {
					Object silhouette = new SilhouetteFactory().create(
							child, ArchiveJob.this.getArooaSession());
					
					finalArchiver.persist(archiveIdentifier.toString(), 
							silhouette, getArooaSession());
				}
				finally {
					OddjobNDC.pop();					
				}
				stopListening(source);
			}
			
			private void stopListening(Stateful source) {
				source.removeStateListener(this);
				listener = null;
				logger().debug("Listener removed.");				
			}
		};
		
		((Stateful) child).addStateListener(listener);
		
		child.run();
	}

	/**
	 * Implementation for a typical stop. 
	 * <p>
	 * This stop implementation doesn't check that the job is 
	 * executing as stop messages must cascade down the hierarchy
	 * to manually started jobs.
	 * 
	 * @throws FailedToStopException 
	 */
	public void stop() throws FailedToStopException {
		stateHandler.assertAlive();
		
		if (!stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
			public void run() {
				stop = true;
			}					
		})) {
			return;
		}

		logger().info("[" + this + "] Stop requested.");
		
		iconHelper.changeIcon(IconHelper.STOPPING);
		try {
			childHelper.stopChildren();			
		} catch (RuntimeException e) {
			iconHelper.changeIcon(IconHelper.EXECUTING);
			throw e;
		}
		
		synchronized (this) {
			notifyAll();
		}
		
		new StopWait(this).run();
		
		stopListening();
		
		logger().info("[" + this + "] Stopped.");
	}
		
	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
			public void run() {
			
				logger().debug("[" + ArchiveJob.this + "] Propergating Soft Reset to children.");			
				
				stopListening();
				childHelper.softResetChildren();
				stop = false;
				getStateChanger().setState(JobState.READY);
				
				logger().info("[" + ArchiveJob.this + "] Soft Reset.");
			}
		});	
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		
		return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
			public void run() {
				logger().debug("[" + ArchiveJob.this + "] Propergating Hard Reset to children.");			
				
				stopListening();
				childHelper.hardResetChildren();
				stop = false;
				getStateChanger().setState(JobState.READY);
				
				logger().info("[" + ArchiveJob.this + "] Hard Reset.");
			}
		});
	}

	private void stopListening() {
		
		StateListener listener = this.listener;
		this.listener = null;
		
		if (listener != null) {
			Stateful child = (Stateful) childHelper.getChild();
			child.removeStateListener(listener);
			logger().debug("Archiving Listener removed from child");
		}		
	}
	
	public Object getArchiveIdentifier() {
		return archiveIdentifier;
	}

	public void setArchiveIdentifier(Object archive) {
		this.archiveIdentifier = archive;
	}

	public String getArchiveName() {
		return archiveName;
	}

	public void setArchiveName(String path) {
		this.archiveName = path;
	}

	public OddjobPersister getArchiver() {
		return archiver;
	}

	public void setArchiver(OddjobPersister archiver) {
		this.archiver = archiver;
	}
	
	/**
	 * Add a listener. The listener will immediately receive add
	 * notifications for all existing children.
	 * 
	 * @param listener The listener.
	 */	
	public void addStructuralListener(StructuralListener listener) {
		stateHandler.assertAlive();
		
		childHelper.addStructuralListener(listener);
	}
	
	/**
	 * Remove a listener.
	 * 
	 * @param listener The listener.
	 */
	public void removeStructuralListener(StructuralListener listener) {
		childHelper.removeStructuralListener(listener);
	}	
			
	/**
	 * @oddjob.property job
	 * @oddjob.description The child job.
	 * @oddjob.required No, but pointless if missing.
	 * 
	 * @param job
	 */
	@ArooaComponent
	public void setJob(Runnable job) {
		if (job == null) {
			childHelper.removeAllChildren();
		}
		else {
			childHelper.insertChild(0, job);
		}
	}
	
	/**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
		s.writeObject(getName());
		if (loggerName().startsWith(getClass().getName())) {
			s.writeObject(null);
		}
		else {
			s.writeObject(loggerName());
		}
		s.writeObject(stateHandler.lastStateEvent());
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		String name = (String) s.readObject();
		logger((String) s.readObject());
		StateEvent savedEvent = (StateEvent) s.readObject();
		
		completeConstruction();
		
		setName(name);
		stateHandler.restoreLastJobStateEvent(savedEvent);
		iconHelper.changeIcon(
				StateIcons.iconFor(stateHandler.getState()));
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
			throw new IllegalStateException("[" + ArchiveJob.this + " Failed set state DESTROYED");
		}
		logger().debug("[" + ArchiveJob.this + "] destroyed.");				
	}
}
