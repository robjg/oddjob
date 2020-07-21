package org.oddjob.jobs.structural;

import org.oddjob.FailedToStopException;
import org.oddjob.Loadable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.*;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.parsing.*;
import org.oddjob.arooa.registry.*;
import org.oddjob.arooa.runtime.PropertyManager;
import org.oddjob.arooa.standard.StandardArooaParser;
import org.oddjob.arooa.standard.StandardPropertyManager;
import org.oddjob.arooa.utils.ListenerSupportBase;
import org.oddjob.arooa.utils.RootConfigurationFileCreator;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.designer.components.ForEachRootDC;
import org.oddjob.framework.ExecutionWatcher;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.io.ExistsJob;
import org.oddjob.scheduling.ExecutorThrottleType;
import org.oddjob.state.*;
import org.oddjob.util.Restore;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Stream;


/**
 * @oddjob.description A job which executes its child jobs for
 * each of the provided values. The child job can access the current
 * value using the pseudo property 'current' to gain access to the
 * current value. The pseudo property 'index' provides a 0 based number for
 * the instance.
 * <p>
 * The return state of this job depends on the return state
 * of the children (like {@link SequentialJob}). Hard resetting this job
 * will cause the children to be destroyed and recreated on the next run
 * (with possibly new values). Soft resetting this job will reset the
 * children but when re-run will not reconfigure the values.
 * <p>
 * As yet There is no persistence for child jobs.
 * <p>
 * It is not possible to reference the internal jobs via their id from
 * outside the foreach job, but within
 * the foreach internal configuration they can reference each other and
 * themselves via their ids.
 * <p>
 * @oddjob.example For each of 3 values.
 * <p>
 * {@oddjob.xml.resource org/oddjob/jobs/structural/ForEachWithIdsExample.xml}
 * <p>
 * The internal configuration is:
 * <p>
 * {@oddjob.xml.resource org/oddjob/jobs/structural/ForEachEchoColour.xml}
 * <p>
 * Unlike other jobs, a job in a for each has it's name configured when it is
 * loaded, before it is run. The job references its self using its id.
 * <p>
 * This example will display the following on the console:
 * <pre>
 * I'm number 0 and my name is Red
 * I'm number 1 and my name is Blue
 * I'm number 2 and my name is Green
 * </pre>
 * @oddjob.example For each of 3 files. The 3 files <code>test1.txt</code>,
 * <code>test2.txt</code> and <code>test3.txt</code> are
 * copied to the <code>work/foreach directory</code>. The oddjob argument
 * <code>${this.args[0]}</code> is so that a base directory can be passed
 * in as part of the unit test for this example.
 * <p>
 * {@oddjob.xml.resource org/oddjob/jobs/structural/ForEachFilesExample.xml}
 * <p>
 * Also {@link ExistsJob} has a similar example.
 * @oddjob.example Executing children in parallel. This example uses a
 * {@link ExecutorThrottleType} to limit the number of parallel
 * executions to three.
 * <p>
 * {@oddjob.xml.resource org/oddjob/jobs/structural/ForEachParallelExample.xml}
 * @oddjob.example Using an execution window. Only the configuration for two jobs will be
 * pre-loaded, and only the last three complete jobs will remain loaded.
 * <p>
 * {@oddjob.xml.resource org/oddjob/jobs/structural/ForEachExecutionWindow.xml}
 */
public class ForEachJob extends StructuralJob<Object>
        implements Stoppable, Loadable, ConfigurationOwner, BeanDirectoryOwner {
    private static final long serialVersionUID = 200903212011060700L;

    /**
     * Root element for configuration.
     */
    public static final ArooaElement FOREACH_ELEMENT =
            new ArooaElement("foreach");

    /**
     * @oddjob.property values
     * @oddjob.description Any value.
     * @oddjob.required No.
     */
    private transient Iterable<? extends Object> values;

    /**
     * The current iterator.
     */
    private transient Iterator<? extends Object> iterator;

    /**
     * @oddjob.property
     * @oddjob.description The number of values to pre-load configurations for.
     * This property can be used with large sets of values to ensure that only a
     * certain number are pre-loaded before execution starts.
     * <p>
     * Setting this property to 0 means that all configuration will be
     * initially loaded.
     * @oddjob.required No. Defaults to all configurations being loaded first.
     */
    private int preLoad;

    /**
     * @oddjob.property
     * @oddjob.description The number of completed jobs to keep. Oddjob configurations
     * can be quite memory intensive, mainly due to logging, purging complete jobs
     * will stop too much memory being taken.
     * <p>
     * Setting this property to 0
     * means that no complete jobs will be purged.
     * @oddjob.required No. Defaults to no complete jobs being purged.
     */
    private int purgeAfter;

    /**
     * @oddjob.property
     * @oddjob.description The configuration that will be parsed
     * for each value.
     * @oddjob.required Yes.
     */
    private transient ArooaConfiguration configuration;

    /**
     * The configuration file.
     */
    private File file;

    /**
     * Support for configuration modification.
     */
    private transient volatile ConfigurationOwnerSupport configurationOwnerSupport;

    /**
     * @oddjob.property
     * @oddjob.description The current value
     * @oddjob.required R/O.
     */
    private transient volatile Object current;

    /**
     * @oddjob.property
     * @oddjob.description The current index in the
     * values.
     * @oddjob.required R/O.
     */
    private transient volatile int index;

    /**
     * Track configuration so they can be destroyed.
     */
    private transient ChildTracking childTracking;

    /**
     * List of jobs loaded and ready to execute.
     */
    private transient LinkedList<Runnable> ready;

    /**
     * List of jobs complete and ready to be removed if the purgeAfter
     * property is set.
     */
    private transient LinkedList<Stateful> complete;

    /**
     * @oddjob.property
     * @oddjob.description Should jobs be executed in parallel.
     * @oddjob.required No. Defaults to false.
     */
    private transient boolean parallel;

    /**
     * The executor to use for parallel execution.
     */
    private transient ExecutorService executorService;

    private transient volatile ExecutionWatcher executionWatcher;

    /**
     * The job threads.
     */
    private transient Map<Runnable, Future<?>> jobThreads;


    /**
     * Constructor.
     */
    public ForEachJob() {
        completeConstruction();
    }

    private void completeConstruction() {
        configurationOwnerSupport =
                new ConfigurationOwnerSupport(this);
        executionWatcher =
                new ExecutionWatcher(() -> {
                    stop = false;
                    ForEachJob.super.startChildStateReflector();
                });
    }

    /**
     * Set the {@link ExecutorService}.
     *
     * @param executorService The Executor Service.
     * @oddjob.property executorService
     * @oddjob.description The ExecutorService to use. This will
     * be automatically set by Oddjob.
     * @oddjob.required No.
     */
    @Inject
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * The current value.
     *
     * @return The current value.
     */
    public Object getCurrent() {
        return current;
    }

    /**
     * Set the values to iterate over.
     *
     * @param values The values.
     */
    public void setValues(Iterable<?> values) {
        this.values = values;
    }

    @Override
    protected StateOperator getInitialStateOp() {
        return new StateOperator() {
            final StateOperator anyStateOp = new AnyActiveStateOp();

            @Override
            public StateEvent evaluate(StateEvent... states) {
                if (states.length == 0) {
                    return new StateEvent(ForEachJob.this,
                            ParentState.COMPLETE);
                } else {
                    return anyStateOp.evaluate(states);
                }
            }
        };
    }

    /*
     * (non-Javadoc)
     * @see org.oddjob.arooa.parsing.ConfigurationOwner#provideConfigurationSession()
     */
    @Override
    public ConfigurationSession provideConfigurationSession() {

        return configurationOwnerSupport.provideConfigurationSession();
    }

    /*
     * (non-Javadoc)
     * @see org.oddjob.arooa.parsing.ConfigurationOwner#addOwnerStateListener(org.oddjob.arooa.parsing.OwnerStateListener)
     */
    @Override
    public void addOwnerStateListener(OwnerStateListener listener) {
        configurationOwnerSupport.addOwnerStateListener(listener);
    }

    /*
     * (non-Javadoc)
     * @see org.oddjob.arooa.parsing.ConfigurationOwner#removeOwnerStateListener(org.oddjob.arooa.parsing.OwnerStateListener)
     */
    @Override
    public void removeOwnerStateListener(OwnerStateListener listener) {
        configurationOwnerSupport.removeOwnerStateListener(listener);
    }

    @Override
    public SerializableDesignFactory rootDesignFactory() {
        return new ForEachRootDC();
    }

    @Override
    public ArooaElement rootElement() {
        return FOREACH_ELEMENT;
    }

    /**
     * Load a configuration for a single value.
     *
     * @param value The value
     * @return The root component in the child configuration.
     * @throws ArooaParseException If the config can't be parsed.
     */
    protected Object loadConfigFor(Object value) throws ArooaParseException {

        logger().debug("Creating child for [" + value + "]");

        ArooaSession existingSession = getArooaSession();

        BeanRegistry psudoRegistry = new LinkedBeanRegistry(
                existingSession);

        RegistryOverrideSession session = new RegistryOverrideSession(
                existingSession, psudoRegistry);

        LocalBean seed = new LocalBean(index++, value);

        StandardArooaParser parser = new StandardArooaParser(seed,
                session);
        parser.setExpectedDocumentElement(FOREACH_ELEMENT);

        ConfigurationHandle handle = parser.parse(configuration);

        Object root = seed.job;

        if (root == null) {
            logger().info("No child job created.");
            return null;
        }

        if (root instanceof Stateful) {
            ((Stateful) root).addStateListener(event -> {
                Stateful source = event.getSource();
                State state = event.getState();

                if (state.isReady()) {
                    ready.add((Runnable) source);
                }

                if (state.isComplete()) {
                    ready.remove(source);
                    complete.add(source);
                }
            });
        } else {
            throw new UnsupportedOperationException("Job " + root +
                    " not Stateful.");
        }

        childTracking.add(root, seed, handle);

        // Must happen after configure so we see the correct value
        // in the job tree.
        childHelper.addChild(root);

        return root;
    }

    /**
     * Remove a child and clear up it's configuration.
     *
     * @param child The child.
     */
    private void remove(Object child) {

        childTracking.remove(child).getConfigurationHandle().getDocumentContext().getRuntime().destroy();
    }

    @Override
    public BeanDirectory provideBeanDirectory() {
        return new SimpleBeanDirectory(
                childTracking::getBean,
                childTracking::getId,
                childTracking::beans,
                getArooaSession().getTools().getPropertyAccessor(),
                getArooaSession().getTools().getArooaConverter());
    }

    /**
     * Setup and load the first jobs.
     * <p>
     * if {@link #preLoad} is 0 all will be loaded otherwise up to
     * that number will be loaded.
     *
     * @throws ArooaParseException
     */
    protected void preLoad() throws ArooaParseException {

        if (configuration == null) {
            throw new IllegalStateException("No configuration.");
        }
        if (getArooaSession() == null) {
            throw new NullPointerException("No ArooaSession.");
        }

        // already loaded?
        if (childTracking != null) {
            return;
        }

        configurationOwnerSupport.setConfigurationSession(
                new ForeachConfigurationSession());

        logger().debug("Creating children from configuration.");

        childTracking = new ChildTracking();
        ready = new LinkedList<>();
        complete = new LinkedList<>();
        jobThreads = new HashMap<>();

        if (values == null) {
            logger().info("No Values.");
            iterator = Collections.emptyList().iterator();
        } else {
            iterator = values.iterator();
        }

        while ((preLoad < 1 || ready.size() < preLoad) &&
                (loadNext() != null)) {
        }
    }

    /**
     * Loads the next Job.
     *
     * @return The next job or null if there isn't one.
     * @throws ArooaParseException
     */
    private Object loadNext() throws ArooaParseException {

        if (iterator.hasNext()) {
            return loadConfigFor(iterator.next());
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.oddjob.Loadable#load()
     */
    @Override
    public void load() {
        try (Restore ignored = ComponentBoundary.push(loggerName(), this)) {
            stateHandler().waitToWhen(new IsNot(StateConditions.RUNNING),
                    () -> {
                        try {
                            if (childTracking != null) {
                                return;
                            }
                            configure();

                            preLoad();
                        } catch (Exception e) {
                            logger().error("Exception executing job.", e);
                            getStateChanger().setStateException(e);
                        }
                    });
        }
    }

    /*
     * (non-Javadoc)
     * @see org.oddjob.Loadable#unload()
     */
    @Override
    public void unload() {
        reset();
    }

    @Override
    public boolean isLoadable() {
        return childTracking == null;
    }

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jobs.AbstractJob#execute()
     */
    protected void execute() throws Exception {

        preLoad();

        executionWatcher.reset();

        List<Object> readyNow = new ArrayList<>(ready);

        for (int i = 0; i < readyNow.size() && !stop; ++i) {

            Object now = readyNow.get(i);

            if (!(now instanceof Runnable)) {
                continue;
            }

            Runnable job = (Runnable) now;

            if (parallel) {

                parallelRun(executionWatcher, job);
            } else {

                final Runnable runnable = executionWatcher.addJob(job);

                runnable.run();

                // Test we can still execute children.
                if (!new SequentialHelper().canContinueAfter(job)) {
                    logger().info("Job [" + job + "] failed. Can't continue.");
                    break;
                }

                Object next = purgeAndLoad();
                if (next != null) {
                    readyNow.add(next);
                }
            }
        }

        // We need to do this force consistent state transitions.
        // This precludes the situation that all child jobs
        // have completed before the execute method completes
        // so the active state is missed, or that none have started
        // so there is a spurious ready state.
        if (parallel && !stop) {
            stateHandler().waitToWhen(new IsStoppable(), new Runnable() {
                public void run() {
                    getStateChanger().setState(ParentState.ACTIVE);
                }
            });
        }

        executionWatcher.start();
    }

    private synchronized void parallelRun(
            final ExecutionWatcher executionWatcher,
            final Runnable job) {

        Runnable runnable = new Runnable() {
            public void run() {

                job.run();

                if (stop) {
                    return;
                }

                try {
                    Object next = purgeAndLoad();

                    if (next != null && next instanceof Runnable) {
                        parallelRun(executionWatcher, (Runnable) next);
                    }
                } catch (ArooaParseException e) {
                    logger().error("Failed to load Jobs.", e);
                }
            }
        };

        Runnable toSubmit = executionWatcher.addJob(runnable);
        Future<?> future = executorService.submit(toSubmit);

        jobThreads.put(job, future);
    }


    /**
     * Helper method to purge complete jobs (if the <code>purgeAfter</code>
     * property is set) and to load the next jobs to run.
     *
     * @throws ArooaParseException
     */
    private synchronized Object purgeAndLoad() throws ArooaParseException {

        while (purgeAfter > 0 && complete.size() > purgeAfter) {

            remove(complete.removeFirst());
        }

        return loadNext();
    }

    @Override
    protected void startChildStateReflector() {
        // This is started by us so override and do nothing.
    }


    @Override
    protected void onStop() throws FailedToStopException {
        super.onStop();

        Map<Runnable, Future<?>> jobThreads = this.jobThreads;
        if (jobThreads == null) {
            return;
        }

        for (Map.Entry<Runnable, Future<?>> future : jobThreads.entrySet()) {
            future.getValue().cancel(false);
        }

        executionWatcher.stop();
    }

    /**
     * @return Returns the index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * This provides a bean for current properties.
     */
    public class LocalBean implements ArooaSessionAware, BeanDirectoryOwner {

        private final int index;
        private final Object current;

        private volatile ArooaSession session;

        /**
         * The root job. Inject by parsing the nested configuration.
         */
        private volatile Object job;

        private volatile int structuralPosition = -1;
        private volatile ChildInfo ci;

        LocalBean(int index, Object value) {
            this.index = index;
            this.current = value;
        }

        @Override
        public void setArooaSession(ArooaSession session) {
            this.session = session;
        }

        public Object getCurrent() {
            return current;
        }

        public int getIndex() {
            return index;
        }

        @ArooaComponent
        public void setJob(final Object child) {

            // Do this locked so editing can't happen when job is being
            // stopped or reset or suchlike.
            stateHandler().callLocked(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (child == null) {
                        if (job == null) {
                            throw new NullPointerException(
                                    "This is an intermittent bug that I can't fix. " +
                                            "Current index is " + index);
                        }

                        structuralPosition = childHelper.removeChild(job);
                        ci = childTracking.remove(job);
                        jobThreads.remove(job);
                        ready.remove(job);
                    } else {
                        // Replacement after edit.
                        if (structuralPosition != -1) {

                            // Configure the root so we can see the name if it
                            // uses the current value.
                            session.getComponentPool().configure(child);

                            childHelper.insertChild(structuralPosition, child);
                            childTracking.add(child, LocalBean.this, ci.getConfigurationHandle());
                        }
                    }

                    job = child;

                    return null;
                }
            });
        }

        @Override
        public BeanDirectory provideBeanDirectory() {
            return session.getBeanRegistry();
        }
    }

    /**
     * An {@link ArooaSession} that wraps an existing session but provides
     * an overriding {@link BeanRegistry} and {@link ComponentPool}.
     *
     * @author rob
     */
    static class RegistryOverrideSession implements ArooaSession {

        private final ArooaSession existingSession;

        private final BeanRegistry beanDirectory;

        private final ComponentPool componentPool;

        private final PropertyManager propertyManager;

        public RegistryOverrideSession(
                ArooaSession exsitingSession,
                BeanRegistry registry) {
            this.existingSession = exsitingSession;
            this.beanDirectory = registry;
            this.componentPool = new PseudoComponentPool(
                    exsitingSession.getComponentPool());
            this.propertyManager = new StandardPropertyManager(
                    existingSession.getPropertyManager());
        }

        @Override
        public ArooaDescriptor getArooaDescriptor() {
            return existingSession.getArooaDescriptor();
        }

        @Override
        public ComponentPool getComponentPool() {
            return componentPool;
        }

        @Override
        public BeanRegistry getBeanRegistry() {
            return beanDirectory;
        }

        @Override
        public PropertyManager getPropertyManager() {
            return propertyManager;
        }

        @Override
        public ComponentProxyResolver getComponentProxyResolver() {
            return existingSession.getComponentProxyResolver();
        }

        @Override
        public ComponentPersister getComponentPersister() {
            return null;
        }

        @Override
        public ArooaTools getTools() {
            return existingSession.getTools();
        }
    }

    /**
     * A {@link ComponentPool} that overrides an existing pool.
     *
     * @author rob
     */
    static class PseudoComponentPool extends SimpleComponentPool {

        private final ComponentPool existingPool;

        public PseudoComponentPool(ComponentPool existingPool) {
            this.existingPool = existingPool;
        }

        @Override
        public Iterable<ComponentTrinity> allTrinities() {
            List<ComponentTrinity> results = new ArrayList<ComponentTrinity>();
            for (ComponentTrinity t : super.allTrinities()) {
                results.add(t);
            }
            for (ComponentTrinity t : existingPool.allTrinities()) {
                results.add(t);
            }
            return results;
        }
    }

    private void reset() {

        if (childTracking == null) {
            return;
        }

        configurationOwnerSupport.setConfigurationSession(null);

        try {
            childHelper.stopChildren();
        } catch (FailedToStopException e) {
            logger().warn("Failed to stop.", e);
        }

        Object[] children = childHelper.getChildren();

        for (Object child : children) {
            remove(child);
        }

        this.childTracking = null;
        this.ready = null;
        this.complete = null;
        this.index = 0;
        this.stop = false;
        this.jobThreads = null;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        reset();
    }

    /**
     * Perform a hard reset on the job.
     */
    public boolean hardReset() {
        try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
            return stateHandler().waitToWhen(new IsHardResetable(), new Runnable() {
                public void run() {
                    stopChildStateReflector();

                    reset();

                    getStateChanger().setState(ParentState.READY);
                    logger().info("Hard Reset complete.");
                }
            });
        }
    }

    /**
     * @return The file name.
     * @oddjob.property stop
     * @oddjob.description The stop flag. This is an internal read only
     * property that is exposed for diagnostic reasons. If a child job
     * does not support stopping then the request to stop may time out but
     * it is useful to know that the stop flag is still set so this job
     * will still stop eventually.
     * @oddjob.required Read Only.
     */
    @Override
    public boolean isStop() {
        return super.isStop();
    }

    /**
     * @return The file name.
     * @oddjob.property file
     * @oddjob.description The name of the configuration file.
     * to use for configuration.
     * @oddjob.required No.
     */
    @ArooaAttribute
    public void setFile(File file) {

        this.file = file;
        if (file == null) {
            this.file = null;
            configuration = null;
        } else {
            new RootConfigurationFileCreator(
                    FOREACH_ELEMENT, NamespaceMappings.empty())
                    .createIfNone(file);
            this.file = file;
            configuration = new XMLConfiguration(file);
        }
    }

    public File getFile() {
        if (file == null) {
            return null;
        }
        return file.getAbsoluteFile();
    }

    public ArooaConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ArooaConfiguration configuration) {
        this.configuration = configuration;
    }

    public int getPreLoad() {
        return preLoad;
    }

    public void setPreLoad(int preLoad) {
        this.preLoad = preLoad;
    }

    public int getPurgeAfter() {
        return purgeAfter;
    }

    public void setPurgeAfter(int purgeAfter) {
        this.purgeAfter = purgeAfter;
    }

    public boolean isParallel() {
        return parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    /*
     * Custom serialization.
     */
    private void writeObject(ObjectOutputStream s)
            throws IOException {
        s.defaultWriteObject();
    }

    /*
     * Custom serialization.
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        completeConstruction();
    }

    /**
     * Only the root foreach should result in a drag point..
     */
    class ForeachConfigurationSession
            extends ListenerSupportBase<SessionStateListener>
            implements ConfigurationSession {

        private final ConfigurationSession mainSession;

        private ConfigurationSession lastModifiedChildSession;

        public ForeachConfigurationSession() {
            this.mainSession = new ConfigConfigurationSession(
                    getArooaSession(), configuration);
        }

        public DragPoint dragPointFor(Object component) {
            // required for the Design Inside action.
            if (component == ForEachJob.this) {
                return mainSession.dragPointFor(component);
            } else {
                for (ConfigurationHandle configHandle :
                        childTracking.configurationHandles()) {

                    ConfigurationSession confSession =
                            new HandleConfigurationSession(configHandle);

                    DragPoint dragPoint = confSession.dragPointFor(component);

                    if (dragPoint != null) {

                        confSession.addSessionStateListener(new SessionStateListener() {

                            @Override
                            public void sessionSaved(ConfigSessionEvent event) {
                                lastModifiedChildSession = null;
                                Iterable<SessionStateListener> listeners = copy();
                                for (SessionStateListener listener : listeners) {
                                    listener.sessionSaved(event);
                                }
                            }

                            @Override
                            public void sessionModified(ConfigSessionEvent event) {
                                lastModifiedChildSession = event.getSource();
                                Iterable<SessionStateListener> listeners = copy();
                                for (SessionStateListener listener : listeners) {
                                    listener.sessionModified(event);
                                }
                            }
                        });

                        return dragPoint;
                    }
                }

                return null;
            }
        }

        public ArooaDescriptor getArooaDescriptor() {
            return mainSession.getArooaDescriptor();
        }

        public void save() throws ArooaParseException {
            if (lastModifiedChildSession != null) {
                lastModifiedChildSession.save();
            } else {
                mainSession.save();
            }
        }

        public boolean isModified() {
            return lastModifiedChildSession != null ||
                    mainSession.isModified();
        }

        public void addSessionStateListener(SessionStateListener listener) {
            super.addListener(listener);
            mainSession.addSessionStateListener(listener);
        }

        public void removeSessionStateListener(SessionStateListener listener) {
            super.removeListener(listener);
            mainSession.removeSessionStateListener(listener);
        }
    }

    static class ChildTracking {

        private final Map<Object, ChildInfo> childInfo = new ConcurrentHashMap<>();

        private final Map<String, Object> idToChild = new ConcurrentHashMap<>();

        private final Map<LocalBean, String> beanToId = new ConcurrentHashMap<>();

        void add(Object child, LocalBean localBean, ConfigurationHandle handle) {
            Objects.requireNonNull(child);

            childInfo.put(child, new ChildInfo(localBean, handle));
            String id = Objects.toString(localBean.current);
            idToChild.put(id, child);
            beanToId.put(localBean, id);
        }

        ChildInfo remove(Object child) {
            ChildInfo childInfo = this.childInfo.remove(child);
            if (childInfo == null) {
                // Called twice during destroy. Null is fine the second time.
                return null;
            }
            String id = beanToId.remove(childInfo.localBean);
            if (id == null) {
                throw new IllegalStateException("No Id for " + childInfo.localBean);
            }
            idToChild.remove(id);
            return childInfo;
        }

        Stream<LocalBean> beans() {
            return childInfo.values().stream().map(ChildInfo::getLocalBean);
        }

        LocalBean getBean(String id) {
            return Optional.ofNullable(idToChild.get(id)).flatMap(
                    child -> Optional.ofNullable(childInfo.get(child))
                            .map(childInfo -> childInfo.getLocalBean()))
                    .orElse(null);
        }

        String getId(Object localBean) {
            return beanToId.get(localBean);
        }

        public ConfigurationHandle[] configurationHandles() {
            return childInfo.values().stream()
                    .map(ci -> ci.getConfigurationHandle()).toArray(ConfigurationHandle[]::new);
        }
    }

    static class ChildInfo {

        private final LocalBean localBean;

        private final ConfigurationHandle<ArooaContext> configurationHandle;

        ChildInfo(LocalBean localBean, ConfigurationHandle<ArooaContext> configurationHandle) {
            Objects.requireNonNull(localBean);
            Objects.requireNonNull(configurationHandle);

            this.localBean = localBean;
            this.configurationHandle = configurationHandle;
        }

        public LocalBean getLocalBean() {
            return localBean;
        }

        public ConfigurationHandle<ArooaContext> getConfigurationHandle() {
            return configurationHandle;
        }
    }
}

