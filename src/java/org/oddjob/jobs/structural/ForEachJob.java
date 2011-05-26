package org.oddjob.jobs.structural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.oddjob.FailedToStopException;
import org.oddjob.Loadable;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaConfiguration;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.SimpleBeanRegistry;
import org.oddjob.arooa.registry.SimpleComponentPool;
import org.oddjob.arooa.runtime.PropertyManager;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.standard.StandardPropertyManager;
import org.oddjob.framework.StructuralJob;
import org.oddjob.io.ExistsJob;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.JobState;
import org.oddjob.state.SequentialHelper;
import org.oddjob.state.StateOperator;
import org.oddjob.state.WorstStateOp;


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
 * 
 * @oddjob.example
 * 
 * For each of 3 values.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/structural/ForEachWithIdsExample.xml}
 * 
 * The internal configuration is:
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/structural/ForEachEchoColour.xml}
 * 
 * Unlike other jobs, a job in a for each has it's name configured when it is 
 * loaded, before it is run. The job references its self using its id.
 * 
 * @oddjob.example
 * 
 * For each of 3 files.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/structural/ForEachFilesExample.xml}
 * 
 * @oddjob.example
 * 
 * Also {@link ExistsJob} has a similar example.
 */

public class ForEachJob extends StructuralJob<Runnable>
implements Stoppable, Loadable {
    private static final long serialVersionUID = 2009032100L;
	
    /**
     * @oddjob.property values
     * @oddjob.description Any value.
     * @oddjob.required No.
     */
	private transient List<Object> types = new ArrayList<Object>();
    		
	/**
	 * @oddjob.property
	 * @oddjob.description The configuration that will be parsed
	 * for each value.
	 * @oddjob.required Yes.
	 */
    private transient ArooaConfiguration configuration;
    
	/**
	 * @oddjob.property
	 * @oddjob.description The current value
	 * @oddjob.required R/O.
	 */
	private transient Object current;

	/**
	 * @oddjob.property
	 * @oddjob.description The current index in the
	 * values.
	 * @oddjob.required R/O.
	 */
	private transient int index;
		
	/** Used by Loadable. */
    private boolean loadOnly;
    
    /** Caputure the id of this component. */
    private String id;
    
    /** Track configuration so they can be destroyed. */
    private transient List<ConfigurationHandle> configurationHandles;
    
	/**
	 * The current value.
	 * 
	 * @return The current value.
	 */
	public Object getCurrent() {
	    return current;
	}
	
	/**
	 * Add a type. This will be called during parsing by the
	 * handler to add a type for each element.
 	 * 
	 * @param type The type.
	 */
	public void setValues(Object[] values) {
		if (values == null) {
			types = null;
		}	
		else {
			types = Arrays.asList(values);
		}
	}

	@Override
	protected StateOperator getStateOp() {
		return new WorstStateOp();
	}

	protected void doLoad() throws ArooaParseException {
	    logger().debug("Creating children from configuration.");
	    
		ArooaSession existingSession = getArooaSession();
		
		configurationHandles = new ArrayList<ConfigurationHandle>();
		
		// load child jobs for each value
		for (int index = 0; index < types.size(); ++index) {
			Object value = types.get(index);
			
			logger().debug("creating child for [" + value + "]");
			
			PsudoRegistry psudoRegistry = new PsudoRegistry(
					existingSession.getBeanRegistry(),
					existingSession.getTools().getPropertyAccessor(),
    				existingSession.getTools().getArooaConverter(),
					index, 
					value);

			RegistryOverrideSession session = new RegistryOverrideSession(
					existingSession, psudoRegistry);
			
			StandardFragmentParser parser = new StandardFragmentParser(
					session);
			parser.setArooaType(ArooaType.COMPONENT);
			
			ConfigurationHandle handle = parser.parse(configuration);
			configurationHandles.add(handle);			
			
		    childHelper.insertChild(childHelper.size(), 
		    		(Runnable) parser.getRoot());
		}
	}
	
	public void load() {
		stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
			public void run() {
			    try {
			    	loadOnly = true;
			    	ForEachJob.this.run();
			    } finally {
			    	loadOnly = false;
			    }
			}
		});
	};
	
	@Override
	public void unload() {
		reset();
	}
	
	public boolean isLoadable() {
		return configurationHandles == null;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected void execute() throws Exception {
		if (types == null) {
			throw new IllegalStateException("No values supplied.");
		}
		if (configuration == null) {
			throw new IllegalStateException("No configuration.");
		}
		if (configurationHandles == null) {
			doLoad();
		}
		if (loadOnly) {
			return;
		}

		// execute
		Runnable[] children = childHelper.getChildren(
				new Runnable[0]);
		
		for (index = 0; index < types.size() && !stop; ++index) {
		    current = types.get(index);
		    
			Runnable job = children[index];
			job.run();
			
			// Test we can still execute children.
			if (!new SequentialHelper().canContinueAfter(job)) {				
				logger().info("Job [" + job + "] failed. Can't continue.");
				break;
			}			
		}	
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
    public static class LocalBean {
    	private final int index;
    	private final Object current;
    	LocalBean (int index, Object value) {
    		this.index = index;
    		this.current = value;
    	}
    	public Object getCurrent() {
    		return current;
    	}
    	public int getIndex() {
    		return index;
    	}
    }

    class RegistryOverrideSession implements ArooaSession {

    	private final ArooaSession existingSession;
    	
    	private final BeanRegistry beanDirectory;
    	
    	private final ComponentPool componentPool;
    	
    	private final PropertyManager propertyManager;
    	
    	public RegistryOverrideSession(
    			ArooaSession exsitingSession,
    			BeanRegistry registry) {
    		this.existingSession = exsitingSession;
    		this.beanDirectory = registry;
    		this.componentPool = new SimpleComponentPool();
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
    
    class PsudoRegistry extends SimpleBeanRegistry {
    	private final BeanDirectory existingDirectory;    	
    	    	
    	PsudoRegistry(BeanDirectory existingDirectory,
    			PropertyAccessor propertyAccessor,
    			ArooaConverter converter,
    			int index, 
    			Object value) {
    		super(propertyAccessor, converter);
    		this.existingDirectory = existingDirectory;
    		if (id != null) {
        		register(id, new LocalBean(index, value));    			
    		}
    	}
    			
		/**
		 * First try our local registry then the parent.
		 * 
		 */
    	public Object lookup(String path) {
			Object component = super.lookup(path);
			if (component == null) {
				return existingDirectory.lookup(path);
			}
			return component;
    	}
    	
    	@Override
    	public <T> T lookup(String path, Class<T> required)
    			throws ArooaConversionException {
			T component = super.lookup(path, required);
			if (component == null) {
				return existingDirectory.lookup(path, required);
			}
			return component;
    	}
    	
    	
    	
		/**
		 * This stops serialisation working for child components. Need to revisit
		 * this if it becomes a requirement!
		 */
		public String getIdFor(Object component) {
			return null;
		}		
    }
    
	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	private void reset() {
		
	    List<ConfigurationHandle> configurationHandles = 
	    	this.configurationHandles;
	    this.configurationHandles = null;
	    
	    if (configurationHandles == null) {
			return;
		}
		
		try {
			childHelper.stopChildren();
		} catch (FailedToStopException e) {
			logger().warn(e);
		}
		
		for (ConfigurationHandle handle : configurationHandles) {
			handle.getDocumentContext().getRuntime().destroy();
		}
		
		childHelper.removeAllChildren();
	}

	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
			public void run() {
				childStateReflector.stop();
				
				reset();
				
				stop = false;
				
				getStateChanger().setJobState(JobState.READY);
				logger().info("[" + ForEachJob.this + "] Hard Reset." );
			}
		});
	}

	public ArooaConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ArooaConfiguration configuration) {
		this.configuration = configuration;
	}
}

