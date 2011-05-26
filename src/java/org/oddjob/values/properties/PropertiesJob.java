package org.oddjob.values.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.oddjob.Describeable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.runtime.PropertyManager;

/**
 * @oddjob.description Creates properties that can used to configure 
 * other jobs.
 * <p>
 * There are four ways to set properties:
 * <ol>
 * <li>As Property name/value Pairs in the values property of this job.</li>
 * <li>By defining the environment attribute to be the prefix to which all
 * environment variables will be appended as properties.</li>
 * <li>By using the sets property to provide a number of addition property
 * sets which are likely to be a reference to properties defined elsewhere.</li>
 * <li>By defining the Input property to be a File/Resource or some other 
 * type of input.</li>
 * </ol>
 * Combinations are possible and the order of evaluation is 
 * as above. Oddjob will do it's usual property substitution using previously
 * defined property values if required. 
 * <p>
 * If the substitute property is defined, property values within the input
 * file will be evaluated for substitution.
 * <p>
 * The Properties job and {@link PropertiesType} type are very similar, the difference
 * between them is that the job defines properties for Oddjob and the type provides 
 * properties for configuring a single job (which could be the
 * sets property of the property job).
 * <p>
 * 
 * @oddjob.example
 * 
 * Defining and using a property. Note the escape syntax for property 
 * expansion.
 * 
 * {@oddjob.xml.resource org/oddjob/values/properties/PropertiesJobFromValues.xml}
 * 
 * @oddjob.example
 * 
 * Loading properties from a class path resource.
 * 
 * {@oddjob.xml.resource org/oddjob/values/properties/PropertiesJobFromInput.xml}
 * 
 * @oddjob.example
 * 
 * Overriding Properties. Normally setting a property is first come first
 * set. Using the override property on the properties job makes the properties 
 * defined in that job take priority.
 * 
 * {@oddjob.xml.resource org/oddjob/values/properties/PropertiesJobOverriding.xml}
 * 
 * @oddjob.example
 * 
 * Capturing Environment Variables. Note that the case sensitivity of 
 * environment variables is Operating System dependent. On Windows 
 * <code>${env.Path}</code> and <code>${env.path}</code> would also yield the
 * same result. On Unix (generally) only <code>${env.PATH}</code> will work.
 * 
 * {@oddjob.xml.resource org/oddjob/values/properties/PropertiesJobEnvironment.xml}
 */
public class PropertiesJob extends PropertiesJobBase
implements Describeable {
	private static final long serialVersionUID = 2011033000L;
		
	/** Delegate to properties base for implementation. (Because we
	 * can't inherit from it as we do with PropertiesType). */
	private transient PropertiesBase delegate;

	private transient ArooaSession session;
	
	/** Prefix environment variables. */
	private String environment;
	
	private boolean override;
	
	/**
	 * Default Constructor.
	 */
	public PropertiesJob() {
		completeConstruction();
	}
	
	/**
	 * Post construction and deserialisation.
	 */
	private void completeConstruction() {
		
		delegate = new PropertiesBase();		
	}
	
	@Override
	public void setArooaContext(ArooaContext context) {
		super.setArooaContext(context);

		delegate.setArooaContext(context);
		
		session = ((PropertiesConfigurationSession) 
				context.getSession()).getOriginal(); 
	}

	@Override
	protected ArooaSession getArooaSession() {
		return session;
	}
	
	/**
	 * Adds the property lookup to the session.
	 */
	protected void addPropertyLookup() {
		PropertyManager propertyManager = session.getPropertyManager();
		if (override) {
			if (environment != null) {
				propertyManager.addPropertyOverride(
						new EnvVarPropertyLookup(environment));
			}
		}
		else {
			if (environment != null) {
				propertyManager.addPropertyLookup(
						new EnvVarPropertyLookup(environment));
			}
		}
		super.addPropertyLookup();
	}
	
	
	
	@Override
	protected int execute() throws IOException, ArooaConversionException {

				
		setProperties(delegate.toProperties());

		// All the work's been done during configuration. This is all
		// we have left to do.		
		addPropertyLookup();
		
		return 0;
	}

	
	@Override
	public Map<String, String> describe() {
		Properties properties = getProperties();
		Map<String, String> copy = new TreeMap<String, String>();
		
		if (properties  == null) {
			return copy;
		}
		
		for (Object key : properties.keySet()) {
			String name = key.toString();
			copy.put(name, properties.getProperty(name));
		}
		
		return copy;
	}
	
	/**
	 * Getter for environment prefix.
	 * 
	 * @return The environment prefix. May be null.
	 */
	public String getEnvironment() {
		return environment;
	}

    /**
     * @oddjob.property environment
     * @oddjob.description The prefix for environment variables.
     * @oddjob.required No.
     */
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

    /**
     * @oddjob.property input
     * @oddjob.description An input source for Properties.
     * @oddjob.required No.
     */
	public void setInput(InputStream input) {
		this.delegate.setInput(input);
	}
	
    /**
     * @oddjob.property fromXML
     * @oddjob.description If the input for the properties is in XML format.
     * @oddjob.required No, default to false.
     */
	public void setFromXML(boolean fromXML) {
		this.delegate.setFromXML(fromXML);
	}
	
	/**
	 * Getter for fromXML.
	 * 
	 * @return true/false.
	 */
	public boolean isFromXML() {
		return this.delegate.isFromXML();
	}
	
    /**
     * @oddjob.property values
     * @oddjob.description Properties defined as key value pairs.
     * @oddjob.required No.
     */
	public void setValues(String key, String value) {
		this.delegate.setValues(key, value);
	}
	
    /**
     * @oddjob.property sets
     * @oddjob.description Extra properties to be merged into the overall
     * property set.
     * @oddjob.required No.
     */
	public void setSets(int index, Properties props) {
		this.delegate.setSets(index, props);
	}
	
	
	/**
	 * Indexed getter for sets.
	 * 
	 * @param index The index.
	 * @return The properites.
	 */
	public Properties getSets(int index) {
		return this.delegate.getSets(index);
	}
	
    /**
     * @oddjob.property substitute
     * @oddjob.description Use substitution for the values of ${} type 
     * properties.
     * @oddjob.required No.
     */
	public void setSubstitute(boolean substitute) {
		this.delegate.setSubstitute(substitute);
	}

	/**
	 * Getter for substitute.
	 * 
	 * @return true/false.
	 */
	public boolean isSubstitute() {
		return delegate.isSubstitute();
	}
	
	/**
	 * Getter for extract.
	 * 
	 * @return The extract prefix or null.
	 */
	public String getExtract() {
		return delegate.getExtract();
	}

    /**
     * @oddjob.property extract
     * @oddjob.description Extract this prefix form property names. Filters
     * out properties that do not begin with this prefix.
     * @oddjob.required No.
     */
	public void setExtract(String extract) {
		this.delegate.setExtract(extract);
	}

	/**
	 * Getter for prefix.
	 * 
	 * @return The appending prefix or null.
	 */
	public String getPrefix() {
		return this.delegate.getPrefix();
	}

    /**
     * @oddjob.property prefix
     * @oddjob.description Append this prefix to property names.
     * @oddjob.required No.
     */
	public void setPrefix(String prefix) {
		this.delegate.setPrefix(prefix);
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

	public boolean isOverride() {
		return override;
	}

    /**
     * @oddjob.property override
     * @oddjob.description Properties of this job will override any previously
     * set.
     * @oddjob.required No. Default is false.
     */
	public void setOverride(boolean override) {
		this.override = override;
	}
}
