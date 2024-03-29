package org.oddjob.events;

import org.oddjob.Structural;
import org.oddjob.arooa.*;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.parsing.*;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.LinkedBeanRegistry;
import org.oddjob.arooa.registry.SimpleComponentPool;
import org.oddjob.arooa.runtime.PropertyManager;
import org.oddjob.arooa.standard.StandardArooaParser;
import org.oddjob.arooa.standard.StandardPropertyManager;
import org.oddjob.arooa.utils.ListenerSupportBase;
import org.oddjob.arooa.utils.RootConfigurationFileCreator;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.designer.components.ForEachRootDC;
import org.oddjob.state.IsStoppable;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;
import org.oddjob.util.Restore;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * @oddjob.description An Event Source For a variable set of child
 * Event Sources. Required when the list of events to wait for changes dynamically - such as the set of files
 * required to run a job.
 *
 * @oddjob.example
 *
 * Wait for prices to be available to price some fruit trades. This resulted as an experiment in turning Oddjob
 * into a rules engine.
 *
 * {@oddjob.xml.resource org/oddjob/events/example/PricingWhenExample.xml}
 *
 */
public class ForEvents<T> extends EventServiceBase<CompositeEvent<T>>
implements Structural, ConfigurationOwner {

    /** Root element for configuration. */
    public static final ArooaElement FOREACH_ELEMENT = 
    	new ArooaElement("events");
    
	/** Track changes to children and notify listeners. */
	protected transient volatile ChildHelper<Object> childHelper; 

	/**
     * @oddjob.property
     * @oddjob.description Any stream of values.
     * @oddjob.required No.
     */
	private transient Stream<?> values;
    		
	/** The current iterator. */
	private transient Iterator<?> iterator;

	/**
	 * @oddjob.property
	 * @oddjob.description Event Operator to filter events. ANY/ALL.
	 * @oddjob.required No, default to ALL.
	 */
	private volatile EventOperator<T> eventOperator;

	/**
	 * @oddjob.property
	 * @oddjob.description The last event to be passed to a consumer.
	 * @oddjob.required Read only.
	 */
	private volatile CompositeEvent<T> last;

	/**
     * @oddjob.property 
     * @oddjob.description The number of completed jobs to keep. Oddjob configurations
     * can be quite memory intensive, mainly due to logging, purging complete jobs
     * will stop too much memory being taken. 
     * <p>
     * Setting this property to 0
     * means that no complete jobs will be purged.
     *
 	 *
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
    
	/** The configuration file. */
	private File file;
		    
	/** Support for configuration modification. */
	private transient volatile ConfigurationOwnerSupport configurationOwnerSupport;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The current index in the
	 * values.
	 * @oddjob.required R/O.
	 */
	private transient AtomicInteger index;
		
    /** Track configuration so they can be destroyed. */
    private transient Map<Object, ConfigurationHandle<ArooaContext>> configurationHandles;
    
    
    
    /**
     * Constructor.
     */
    public ForEvents() {
    	completeConstruction();
	}
    
	private void completeConstruction() {
		childHelper = new ChildHelper<>(this);
		configurationOwnerSupport =
			new ConfigurationOwnerSupport(this);
		index = new AtomicInteger();
	}

		
	/**
	 * Add a type. This will be called during parsing by the
	 * handler to add a type for each element.
 	 * 
	 * @param values The type.
	 */
	public void setValues(Stream<?> values) {
		this.values = values;
	}

	/**
	 * Add a listener. The listener will immediately receive add
	 * notifications for all existing children.
	 * 
	 * @param listener The listener.
	 */	
	public void addStructuralListener(StructuralListener listener) {		
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
	 * @param value
	 * @throws ArooaParseException
	 */
	protected Object loadConfigFor(Object value) throws ArooaParseException {
		
		logger().debug("Creating child for [" + value + "]");
		
		ArooaSession existingSession = getArooaSession();
		
		BeanRegistry pseudoRegistry = new LinkedBeanRegistry(
				existingSession);

		RegistryOverrideSession session = new RegistryOverrideSession(
				existingSession, pseudoRegistry);
		
		LocalBean seed = new LocalBean(index.incrementAndGet(), value);
		
		StandardArooaParser parser = new StandardArooaParser(seed,
				session);
		parser.setExpectedDocumentElement(FOREACH_ELEMENT);
		
		ConfigurationHandle<ArooaContext> handle = parser.parse(configuration);
		
		Object root = seed.job;

		if (root == null) {
			logger().info("No child job created.");
			return null;
		}

		configurationHandles.put(root, handle);			
		
		// Configure the root, so we can see the name if it
	    // uses the current value.
		seed.session.getComponentPool().configure(root);
		
		// Must happen after configure, so we see the correct value
		// in the job tree.
		childHelper.addChild(root);
		
	    return root;
	}

	/**
	 * Remove a child and clear up its configuration.
	 * 
	 * @param child The child.
	 */
	private void remove(Object child) {
		
		ConfigurationHandle<ArooaContext> handle = configurationHandles.get(child);
		handle.getDocumentContext().getRuntime().destroy();
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
		if (configurationHandles != null) {
			return;
		}
		
        configurationOwnerSupport.setConfigurationSession(
				new ForeachConfigurationSession());
        
	    logger().debug("Creating children from configuration.");
	    
		configurationHandles = new HashMap<>();
		
		if (values == null) {
			logger().info("No Values.");
			iterator = Collections.emptyIterator();
		}
		else {
			iterator = values.iterator();
		}
		
		while (true) {
			if (loadNext() == null) break;
		}
	}
	
	/**
	 * Loads the next Job.
	 * 
	 * @return The next job or null if there isn't one.
	 * 
	 * @throws ArooaParseException
	 */
	private Object loadNext() throws ArooaParseException {
		
		if (iterator.hasNext()) {
			return loadConfigFor(iterator.next());
		}
		else {
			return null;
		}
	}	
	
	
	@Override
	public Restore doStart(Consumer<? super CompositeEvent<T>> consumer) {

        EventOperator<T> eventOperator = Optional.ofNullable(this.eventOperator).orElse(new AllEvents<>());

		try {
			preLoad();
		} catch (ArooaParseException e) {
			stateHandler().waitToWhen(new IsStoppable(), 
					() -> getStateChanger().setStateException(e));
		}

		List<EventSource<?>> subscribeNodes = new ArrayList<>();
		
		for (Object child : childHelper) {
			subscribeNodes.add(EventSourceAdaptor.maybeEventSourceFrom(child, getArooaSession())
					.orElseThrow(() -> new IllegalStateException("Child [" +
							child + "] is not able to be an Event Source")));
		}

		return eventOperator.start(subscribeNodes,
				list -> {
                    last = list;
                    try {
                        save();
                    } catch (ComponentPersistException e) {
                        throw new RuntimeException(e);
                    }
                    consumer.accept(list);
                });
	}
			
    /**
     * @return Returns the index.
     */
    public int getIndex() {
        return index.get();
    }

    public EventOperator<T> getEventOperator() {
        return eventOperator;
    }

    @ArooaAttribute
    public void setEventOperator(EventOperator<T> eventOperator) {
        this.eventOperator = eventOperator;
    }

    public CompositeEvent<T> getLast() {
        return last;
    }

    /**
     * This provides a bean for current properties.
     */
    public class LocalBean implements ArooaSessionAware {
    	
    	private final int index;
    	private final Object current;

    	private volatile ArooaSession session;
  
    	/** The root job. Inject by parsing the nested configuration. */
    	private volatile Object job;
    	
    	private volatile int structuralPosition = -1;
    	private volatile ConfigurationHandle<ArooaContext> handle;
    	
    	LocalBean (int index, Object value) {
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
    		stateHandler().runLocked(() -> {
				if (child == null) {
					if (job == null) {
						throw new NullPointerException(
								"This is an intermittent bug that I can't fix. " +
								"Current index is " + index);
					}

					structuralPosition = childHelper.removeChild(job);
					handle = configurationHandles.remove(job);
				}
				else {
					// Replacement after edit.
					if (structuralPosition != -1) {

						// Configure the root, so we can see the name if it
						// uses the current value.
						session.getComponentPool().configure(child);

						childHelper.insertChild(structuralPosition, child);
						configurationHandles.put(child, handle);
					}
				}

				job = child;
			});
	    }
    }

    /**
     * An {@link ArooaSession} that wraps an existing session but provides
     * an overriding {@link BeanRegistry} and {@link ComponentPool}.
     * 
     * @author rob
     *
     */
    static class RegistryOverrideSession implements ArooaSession {

    	private final ArooaSession existingSession;
    	
    	private final BeanRegistry beanDirectory;
    	
    	private final ComponentPool componentPool;
    	
    	private final PropertyManager propertyManager;
    	
    	public RegistryOverrideSession(
    			ArooaSession existingSession,
    			BeanRegistry registry) {
    		this.existingSession = existingSession;
    		this.beanDirectory = registry;
    		this.componentPool = new PseudoComponentPool(
    				existingSession.getComponentPool());
    		this.propertyManager = new StandardPropertyManager(
    				this.existingSession.getPropertyManager());
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
     *
     */
    static class PseudoComponentPool extends SimpleComponentPool {
    	
    	private final ComponentPool existingPool;
    	
    	public PseudoComponentPool(ComponentPool existingPool) {
    		this.existingPool = existingPool;
		}
    	
    	@Override
    	public Iterable<ComponentTrinity> allTrinities() {
			List<ComponentTrinity> results = new ArrayList<>();
			for (ComponentTrinity t : super.allTrinities()) {
				results.add(t);
			}
			for (ComponentTrinity t : existingPool.allTrinities()) {
				results.add(t);
			}
			return results;
    	}
    }

    @Override
	protected void onReset() {
		
	    if (configurationHandles == null) {
			return;
		}
		
		configurationOwnerSupport.setConfigurationSession(null);
		
		Object[] children = childHelper.getChildren();
		
		for (Object child : children) {
			remove(child);
		}
		
	    this.configurationHandles = null;
		this.index.set(0);
        this.last = null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
				
		onReset();
	}
	
	/**
	 * @oddjob.property file
	 * @oddjob.description The name of the configuration file.
	 * to use for configuration.
	 * @oddjob.required No.
	 * 
	 * @return The file name.
	 */
	@ArooaAttribute
	public void setFile(File file) {

		if (file == null) {
			configuration = null;
		}
		else {
			new RootConfigurationFileCreator(
					FOREACH_ELEMENT, NamespaceMappings.empty()).createIfNone(file);
			configuration = new XMLConfiguration(file);
		}
		this.file = file;
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


	public int getPurgeAfter() {
		return purgeAfter;
	}

	public void setPurgeAfter(int purgeAfter) {
		this.purgeAfter = purgeAfter;
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
	 * Only the root foreach should result in a drag point.
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
			if (component == ForEvents.this) {
				return mainSession.dragPointFor(component);
			}
			else {
				for (ConfigurationHandle<ArooaContext> configHandle :
							configurationHandles.values()) {
					
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
			}
			else {
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
	
}

