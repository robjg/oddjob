package org.oddjob.values.properties;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.runtime.PropertyLookup;
import org.oddjob.arooa.runtime.PropertyManager;
import org.oddjob.arooa.runtime.RuntimeEvent;
import org.oddjob.arooa.runtime.RuntimeListener;
import org.oddjob.framework.SerializableJob;
import org.oddjob.state.JobState;

/**
 * Base class for jobs that interact with the {@link PropertyManager}.
 * 
 */
abstract public class PropertiesJobBase extends SerializableJob {
	private static final long serialVersionUID = 2011032200L;
		
	/** The properties we're defining. */
	private Properties properties;
	
	/** The property lookup this job defines. */
	private transient PropertyLookup lookup;
	
	/**
	 * Default Constructor.
	 */
	public PropertiesJobBase() {
		completeConstruction();
	}
	
	/**
	 * Post construction and deserialisation.
	 */
	private void completeConstruction() {
		
		lookup = new PropertyLookup() {
			
			@Override
			public String lookup(String propertyName) {
				return properties.getProperty(propertyName);
			}
		};
	}
		
	@Override
	@ArooaHidden
	public void setArooaContext(ArooaContext context) {
		super.setArooaContext(context);

		context.getRuntime().addRuntimeListener(new RuntimeListener() {
			
			@Override
			public void beforeInit(RuntimeEvent event)
					throws ArooaConfigurationException {
			}
			
			@Override
			public void beforeDestroy(RuntimeEvent event)
					throws ArooaConfigurationException {
			}
			
			@Override
			public void beforeConfigure(RuntimeEvent event)
					throws ArooaConfigurationException {
			}
			
			@Override
			public void afterInit(RuntimeEvent event)
					throws ArooaConfigurationException {
				// this should only be set after serialization.
				if (stateHandler.getState() == JobState.COMPLETE) {
					if (properties == null) {
						throw new NullPointerException("This should be impossible.");
					}
					addPropertyLookup();
				}
			}
			
			@Override
			public void afterDestroy(RuntimeEvent event)
					throws ArooaConfigurationException {
			}
			
			@Override
			public void afterConfigure(RuntimeEvent event)
					throws ArooaConfigurationException {
			}
		});
		
	}

	/**
	 * Adds the property lookup to the session.
	 */
	protected void addPropertyLookup() {
		ArooaSession session = getArooaSession();
		if (session == null) {
			throw new NullPointerException("No Session.");
		}
		
		if (isOverride()) {
			session.getPropertyManager().addPropertyOverride(lookup);					
		}
		else {
			session.getPropertyManager().addPropertyLookup(lookup);					
		}
	}

	@Override
	protected void onReset() {
		getArooaSession().getPropertyManager().removePropertyLookup(lookup);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		onReset();
	}
	
	protected void setProperties(Properties properties) {
		this.properties = properties;
	}
	
    /**
     * @oddjob.property properties
     * @oddjob.description Provide all the merged properties defined by this 
     * job.
     * @oddjob.required Read Only.
     */
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		completeConstruction();
	}

	/**
	 * Are the properties overide prperties.
	 * 
	 * @return
	 */
	abstract protected boolean isOverride();

}
