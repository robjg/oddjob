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
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * @oddjob.description An Event Source For a variable set of child
 * Event Sources. Still a work in progress.
 * 
 */
public class ForEvents<T> extends EventSourceBase<T>
implements Structural, ConfigurationOwner {

    /** Root element for configuration. */
    public static final ArooaElement FOREACH_ELEMENT = 
    	new ArooaElement("events");
    
	/** Track changes to children an notify listeners. */
	protected transient volatile ChildHelper<Object> childHelper; 

	/**
     * @oddjob.property values
     * @oddjob.description Any value.
     * @oddjob.required No.
     */
	private transient Stream<? extends Object> values;
    		
	/** The current iterator. */
	private transient Iterator<? extends Object> iterator;

	private volatile EventOperator<T> eventOperator;

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
	private transient volatile int index;
		
    /** Track configuration so they can be destroyed. */
    private transient Map<Object, ConfigurationHandle> configurationHandles;
    
    
    
    /**
     * Constructor.
     */
    public ForEvents() {
    	completeConstruction();
	}
    
	private void completeConstruction() {
		childHelper = new ChildHelper<Object>(this);
		configurationOwnerSupport =
			new ConfigurationOwnerSupport(this);		
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
	@SuppressWarnings("unchecked")
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
		
	    if (! (root instanceof EventSource<?>)) {
	    	throw new UnsupportedOperationException("Job " + root + 
	    			" not a SubscribeNode.");
	    }	    
	    
		configurationHandles.put(root, handle);			
		
		// Configure the root so we can see the name if it 
	    // uses the current value.
		seed.session.getComponentPool().configure(root);
		
		// Must happen after configure so we see the correct value
		// in the job tree.
		childHelper.addChild((EventSource<Object>) root);
		
	    return root;
	}

	/**
	 * Remove a child and clear up it's configuration.
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
	    
		configurationHandles = new HashMap<Object, ConfigurationHandle>();
		
		if (values == null) {
			logger().info("No Values.");
			iterator = Collections.emptyList().iterator();
		}
		else {
			iterator = values.iterator();
		}
		
		while (loadNext() != null);
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
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Restore doStart(Consumer<? super EventOf<T>> consumer) throws Exception {

        EventOperator<T> eventOperator = Optional.ofNullable(this.eventOperator).orElse(new AllEvents<>());

		try {
			preLoad();
		} catch (ArooaParseException e) {
			stateHandler().waitToWhen(new IsStoppable(), 
					() -> getStateChanger().setStateException(e));
		}

		List<EventSource<T>> susbscribeNodes = new ArrayList<>();
		
		for (Object child : childHelper) {
			if (child instanceof EventSource<?>) {
				susbscribeNodes.add((EventSource<T>) child);
			}
		}

		return eventOperator.start(susbscribeNodes,
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
        return index;
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
    	private volatile ConfigurationHandle handle;
    	
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
			    	    handle = configurationHandles.remove(job);
    		    	}
    		    	else {
    		    		// Replacement after edit.
    		    		if (structuralPosition != -1) {
    		    			
    			    		// Configure the root so we can see the name if it 
    			    	    // uses the current value.
    			    		session.getComponentPool().configure(child);
    			    		
    			    	    childHelper.insertChild(structuralPosition, child);
    			    	    configurationHandles.put(child, handle);
    		    		}
    		    	}
    		    	
    		    	job = child;
    		    	
    				return null;
    			}
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
     *
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
		this.index = 0;
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

		this.file = file;
		if (file == null) {
			this.file = null;
			configuration = null;
		}
		else {
			new RootConfigurationFileCreator(
					FOREACH_ELEMENT, NamespaceMappings.empty()).createIfNone(file);
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
			if (component == ForEvents.this) {
				return mainSession.dragPointFor(component);
			}
			else {
				for (ConfigurationHandle configHandle : 
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

