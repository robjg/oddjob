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
import org.oddjob.framework.ComponentBoundry;
import org.oddjob.framework.StopWait;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.ParentState;
import org.oddjob.state.ParentStateChanger;
import org.oddjob.state.ParentStateHandler;
import org.oddjob.state.StandardParentStateConverter;
import org.oddjob.state.State;
import org.oddjob.state.StateChanger;
import org.oddjob.state.StateCondition;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
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

	private static final long serialVersionUID = 2010032500L;
	
	/** Handle state. */
	private transient volatile ParentStateHandler stateHandler;
	
	/** Used to notify clients of an icon change. */
	private transient volatile IconHelper iconHelper;
	
	/** Used to change state. */
	private transient volatile ParentStateChanger stateChanger;
	
	/** Track changes to children an notify listeners. */
	private transient volatile ChildHelper<Runnable> childHelper; 
			
	/**
	 * @oddjob.property 
	 * @oddjob.description The identifier of the snapshot that will
	 * be taken when this job runs.
	 * @oddjob.required Yes.
	 */
	private volatile Object archiveIdentifier;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description The name of the acrhive that all snapshots
	 * will be stored in.
	 * @oddjob.required Yes.
	 */
	private volatile String archiveName;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description The persister to use to store archives.
	 * @oddjob.required Yes, but will fall back on the current Oddjob persister.
	 */
	private transient volatile OddjobPersister archiver;
	
	/** Listener that does the archiving. */
	private transient volatile PersistingStateListener listener;

	/** Stop flag. */
	protected transient volatile boolean stop;
	
	/**
	 * Constructor.
	 */
	public ArchiveJob() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		stateHandler = new ParentStateHandler(this);
		childHelper = new ChildHelper<Runnable>(this);
		iconHelper = new IconHelper(this, 
				StateIcons.iconFor(stateHandler.getState()));
		stateChanger = new ParentStateChanger(stateHandler, iconHelper, 
				new Persistable() {					
					@Override
					public void persist() throws ComponentPersistException {
						save();
					}
				});
	}

	@Override
	protected ParentStateHandler stateHandler() {
		return stateHandler;
	}

	@Override
	protected IconHelper iconHelper() {
		return iconHelper;
	}
	
	protected StateChanger<ParentState> getStateChanger() {
		return stateChanger;
	}
	
	/**
	 * Implement the main execute method for a job. This surrounds the 
	 * doExecute method of the sub class and sets state for the job.
	 */
	public final void run() {
		ComponentBoundry.push(loggerName(), this);
		try {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					getStateChanger().setState(ParentState.EXECUTING);
				}					
			})) {
				return;
			}
			
			logger().info("Executing.");

			try {
				configure();
				
				execute();
			}
			catch (final Throwable e) {
				logger().error("Job Exception.", e);
				
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						getStateChanger().setStateException(e);
					}
				});
			}	
			logger().info("Execution finished.");
		}
		finally {
			ComponentBoundry.pop();
		}
	}
	
	/**
	 * Listen for state changes. Persist when in finished state.
	 * Reflect state for this job.
	 */
	private class PersistingStateListener implements StateListener {

		private final Stateful child;
		
		private final ComponentPersister componentPersister;
		
		private volatile StateEvent event;
		
		private volatile boolean reflect;
		
		public PersistingStateListener(Stateful child,
				ComponentPersister componentPersister) {
			this.child = child;
			this.componentPersister = componentPersister;
		}
		
		void startReflecting() {
			reflect = true;
			reflectState();
		}
		
		void reflectState() {
			if (event.getState().isDestroyed()) {
				stopListening(event.getSource());
			}
			else {
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						if (event.getState().isException()) {
							getStateChanger().setStateException(
									event.getException());
						}
						else {
							getStateChanger().setState(
									new StandardParentStateConverter(
											).toStructuralState(
													event.getState()));
						}
					}
				});
			}
		}
		
		@Override
		public void jobStateChange(final StateEvent event) {

			ComponentBoundry.push(loggerName(), ArchiveJob.this);
			try {
				this.event = event;

				if (reflect) {
					reflectState();
				}

				if (stop) {
					// don't persist when stopping.
					return;
				}

				State state = event.getState();

				StateCondition finished = StateConditions.FINISHED;
				if (finished.test(state)) {

					if (!stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
						public void run() {
							logger().info("Archiving [" + event.getSource() + 
									"] as [" + archiveIdentifier + 
									"] because " + event.getState() + ".");

							try {
								persist(event.getSource());
							}
							catch (ComponentPersistException e) {
								logger().error("Failed to persist.", e);
								getStateChanger().setStateException(e);
							}

						}
					})) {
					}
				}
			} finally {
				ComponentBoundry.pop();
			}
		}
		
		private void persist(Stateful source) throws ComponentPersistException {
			ComponentBoundry.push(loggerName(), ArchiveJob.this);
			try {
				Object silhouette = new SilhouetteFactory().create(
						child, ArchiveJob.this.getArooaSession());
				
				componentPersister.persist(archiveIdentifier.toString(), 
						silhouette, getArooaSession());
			}
			finally {
				ComponentBoundry.pop();					
			}
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
		
		final ComponentPersister componentPersister = 
			archiver.persisterFor(archiveName);
		
		if (componentPersister == null) {
			throw new NullPointerException("No Persister for [" + 
					archiveName + "]");
		}
		
		Runnable child = childHelper.getChild();
		
		if (child == null) {
			return;
		}
		
		if (! (child instanceof Stateful)) {
			throw new IllegalArgumentException("Child must be stateful to be archived.");
		}
		
		if (listener == null) {
			listener = new PersistingStateListener((Stateful) child, 
					componentPersister);		
			((Stateful) child).addStateListener(listener);
		}
		
		child.run();
		
		listener.startReflecting();
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
		
		ComponentBoundry.push(loggerName(), this);
		try {		
			if (!stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
					stop = true;
				}					
			})) {
				return;
			}
	
			logger().info("Stopping.");
			
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
			
			stopListening((Stateful) childHelper.getChild());
			
			logger().info("Stopped.");
		} finally {
			ComponentBoundry.pop();
		}
	}
		
	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		return commonReset(new IsSoftResetable(), "Soft");
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		return commonReset(new IsHardResetable(), "Hard");
	}
	
	/**
	 * Provide common reset functionality. Note that the lock is 
	 * not held over propagation to children because some deadlock
	 * problem was occurring.
	 * 
	 * @param condition
	 * @param text
	 * @return
	 */
	private boolean commonReset(StateCondition condition, final String text) {
		
		ComponentBoundry.push(loggerName(), this);
		try {
			if (!stateHandler.waitToWhen(condition, new Runnable() {
				public void run() {
					logger().debug("Propagating " + text + 
							" reset to children.");
				}
			})) {
				return false;
			}
			
			stopListening((Stateful) childHelper.getChild());
			childHelper.hardResetChildren();

			return stateHandler.waitToWhen(condition, new Runnable() {
				public void run() {
					stop = false;
					getStateChanger().setState(ParentState.READY);
					
					logger().info(text + " Reset complete.");
				}
			});
		} 
		finally {
			ComponentBoundry.pop();
		}
	}

	/**
	 * Stop listening to state changes.
	 *  
	 * @param to The child.
	 */
	private void stopListening(Stateful to) {
		
		if (to == null) {
			return;
		}
		
		StateListener listener = this.listener;
		this.listener = null;
		
		if (listener != null) {
			to.removeStateListener(listener);
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
				stateHandler().setState(ParentState.DESTROYED);
				stateHandler().fireEvent();
			}
		})) {
			throw new IllegalStateException("[" + ArchiveJob.this + "] Failed set state DESTROYED");
		}
		logger().debug("[" + this + "] Destroyed.");				
	}
}
