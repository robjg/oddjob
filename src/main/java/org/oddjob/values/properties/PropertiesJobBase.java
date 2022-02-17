package org.oddjob.values.properties;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.runtime.PropertyLookup;
import org.oddjob.arooa.runtime.PropertyManager;
import org.oddjob.arooa.standard.StandardPropertyLookup;
import org.oddjob.framework.extend.SerializableJob;
import org.oddjob.state.JobState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

/**
 * Base class for jobs that interact with the {@link PropertyManager}.
 * 
 */
abstract public class PropertiesJobBase extends SerializableJob {
	private static final long serialVersionUID = 2011032200L;
		
	/** The properties we're defining. */
	private volatile Properties properties;
	
	/** The property lookup this job defines. */
	private transient PropertyLookup lookup;

	@Override
	protected void onInitialised() {
		super.onInitialised();

		// this should only be set after serialization.
		if (stateHandler().getState() == JobState.COMPLETE) {
			if (properties == null) {
				throw new NullPointerException("This should be impossible.");
			}
			doWhenDeserialized();
		}

	}

	protected void doWhenDeserialized() {
		addPropertyLookup();
	}

	/**
	 * Allow sub classes to join in the lookup creating life cycle.
	 */
	protected void createPropertyLookup() {
		
		lookup = new StandardPropertyLookup(properties, this.toString());
	}
	
	/**
	 * Adds the property lookup to the session.
	 */
	protected final void addPropertyLookup() {
		createPropertyLookup();
		
		ArooaSession session = getArooaSession();
		if (session == null) {
			throw new NullPointerException("No Session.");
		}
		
		if (isOverride()) {
			session.getPropertyManager().addPropertyOverride(
					getLookup());					
		}
		else {
			session.getPropertyManager().addPropertyLookup(
					getLookup());					
		}
	}

	/**
	 * Subclasses can override the lookup.
	 * 
	 * @return
	 */
	protected PropertyLookup getLookup() {
		return lookup;
	}
	
	@Override
	protected void onReset() {
		getArooaSession().getPropertyManager().removePropertyLookup(
				getLookup());
		lookup = null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		onReset();
	}
	
	protected void setProperties(Properties properties) {
		logger().debug("Setting properties: {}", properties);

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
	}

	/**
	 * Are the properties override properties.
	 * 
	 * @return
	 */
	abstract protected boolean isOverride();

}
