package org.oddjob.persist;

import org.oddjob.*;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.framework.extend.BasePrimary;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.framework.util.StopWait;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.state.*;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;
import org.oddjob.util.Restore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author rob
 * @oddjob.description A Job that is capable of taking a snapshot of the
 * state of it's child jobs. An {@link org.oddjob.persist.ArchiveBrowserJob}
 * can be used to browse an archive created with this job.
 * @oddjob.example Create an archive after each scheduled run. The time of the schedule
 * is used to identify the archive.
 * <p>
 * {@oddjob.xml.resource org/oddjob/persist/ArchiveJobTest.xml}
 */
public class ArchiveJob extends BasePrimary
        implements
        Runnable, Serializable,
        Stoppable, Resettable, Stateful, Structural {

    private static final long serialVersionUID = 2010032500L;

    /**
     * Handle state.
     */
    private transient volatile ParentStateHandler stateHandler;

    /**
     * Used to notify clients of an icon change.
     */
    private transient volatile IconHelper iconHelper;

    /**
     * Used to change state.
     */
    private transient volatile ParentStateChanger stateChanger;

    /**
     * Track changes to children an notify listeners.
     */
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
     * @oddjob.description The name of the archive that all snapshots
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

    /**
     * Listener that does the archiving.
     */
    private transient volatile PersistingStateListener listener;

    /**
     * Stop flag.
     */
    protected transient volatile boolean stop;

    /**
     * Constructor.
     */
    public ArchiveJob() {
        completeConstruction();
    }

    private void completeConstruction() {
        stateHandler = new ParentStateHandler(this);
        childHelper = new ChildHelper<>(this);
        iconHelper = new IconHelper(this,
                StateIcons.iconFor(stateHandler.getState()));
        stateChanger = new ParentStateChanger(stateHandler, iconHelper,
                this::save);
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
        try (Restore ignored = ComponentBoundary.push(loggerName(), this)) {
            if (!stateHandler.waitToWhen(new IsExecutable(),
                    () -> getStateChanger().setState(ParentState.EXECUTING))) {
                return;
            }

            logger().info("Executing.");
            try {
                configure();

                execute();
            } catch (final Throwable e) {
                logger().error("Job Exception.", e);

                stateHandler.runLocked(
                        () -> getStateChanger().setStateException(e));
            }
            logger().info("Execution finished.");
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
            } else {
                stateHandler.runLocked(
                        () -> {
                            if (event.getState().isException()) {
                                getStateChanger().setStateException(
                                        event.getException());
                            } else {
                                getStateChanger().setState(
                                        new StandardParentStateConverter(
                                        ).toStructuralState(
                                                event.getState()));
                            }
                        });
            }
        }

        @Override
        public void jobStateChange(final StateEvent event) {

            try (Restore ignored = ComponentBoundary.push(loggerName(), ArchiveJob.this)) {
                this.event = event;

                if (!stop) {
                    // don't persist when stopping.
                    State state = event.getState();

                    StateCondition finished = StateConditions.FINISHED;
                    if (finished.test(state)) {

                        stateHandler.runLocked(() -> {
                            logger().info("Archiving [" + event.getSource() +
                                    "] as [" + archiveIdentifier +
                                    "] because " + event.getState() + ".");

                            try {
                                persist();
                            } catch (ComponentPersistException e) {
                                logger().error("Failed to persist.", e);
                                getStateChanger().setStateException(e);
                            }

                        });
                    }
                }

                // Order is important. Reflect the state to parents after persisting
                if (reflect) {
                    reflectState();
                }

            }
        }

        private void persist() throws ComponentPersistException {

            try (Restore ignored = ComponentBoundary.push(loggerName(), ArchiveJob.this)) {
                Object silhouette = new SilhouetteFactory().create(
                        child, ArchiveJob.this.getArooaSession());

                componentPersister.persist(archiveIdentifier.toString(),
                        silhouette, getArooaSession());
            }
        }
    }

    protected void execute() throws Throwable {

        if (archiveIdentifier == null) {
            throw new NullPointerException("No ArchiveIdentifier.");
        }


        if (archiver == null) {
            ComponentPersister persister =
                    getArooaSession().getComponentPersister();

            if (persister instanceof OddjobPersister) {
                archiver = ((OddjobPersister) persister);
            }
        }

        if (archiver == null) {
            logger().info("No archiver, using in memory one.");
            archiver = new MapPersister();
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

        if (!(child instanceof Stateful)) {
            throw new IllegalArgumentException("Child must be stateful to be archived.");
        }

        if (listener == null) {
            listener = new PersistingStateListener((Stateful) child,
                    componentPersister);
            ((Stateful) child).addStateListener(listener);
        }

        child.run();

        // Todo: We should strictly go to ACTIVE if any ACTIVE state from child otherwise
        // state transitions aren't predictable.
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

        try (Restore ignored = ComponentBoundary.push(loggerName(), this)) {
            if (!stateHandler.waitToWhen(new IsStoppable(),
                    () -> stop = true)) {
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

        try (Restore ignored = ComponentBoundary.push(loggerName(), this)) {
            if (!stateHandler.waitToWhen(condition,
                    () -> logger().debug("Propagating " + text +
                            " reset to children."))) {
                return false;
            }

            stopListening((Stateful) childHelper.getChild());
            childHelper.hardResetChildren();

            return stateHandler.waitToWhen(condition,
                    () -> {
                        stop = false;
                        getStateChanger().setState(ParentState.READY);

                        logger().info(text + " Reset complete.");
                    });
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
     * @param job
     * @oddjob.property job
     * @oddjob.description The child job.
     * @oddjob.required No, but pointless if missing.
     */
    @ArooaComponent
    public void setJob(Runnable job) {
        if (job == null) {
            childHelper.removeAllChildren();
        } else {
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
        } else {
            s.writeObject(loggerName());
        }
        s.writeObject(stateHandler.lastStateEvent().serializable());
    }

    /**
     * Custom serialisation.
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        String name = (String) s.readObject();
        logger((String) s.readObject());
        StateDetail savedEvent =
                (StateDetail) s.readObject();

        completeConstruction();

        setName(name);
        stateHandler.restoreLastJobStateEvent(savedEvent);
        iconHelper.changeIcon(
                StateIcons.iconFor(stateHandler.getState()));
    }

    @Override
    protected void onDestroy() {
        stateHandler.assertAlive();

        super.onDestroy();

        try (Restore ignored = ComponentBoundary.push(loggerName(), this)) {
            stateHandler.waitToWhen(new IsAnyState(), () -> {
                stop = true;
                stopListening((Stateful) childHelper.getChild());
            });
        }
    }

    /**
     * Internal method to fire state.
     */
    protected void fireDestroyedState() {

        if (!stateHandler().waitToWhen(new IsAnyState(), () -> {
            stateHandler().setState(ParentState.DESTROYED);
            stateHandler().fireEvent();
        })) {
            throw new IllegalStateException("[" + ArchiveJob.this + "] Failed set state DESTROYED");
        }
        logger().debug("[" + this + "] Destroyed.");
    }
}
