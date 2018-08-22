package org.oddjob.values.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.oddjob.Describeable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.runtime.PropertyLookup;
import org.oddjob.arooa.runtime.PropertySource;

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
 * If the substitute property is true, property values will be evaluated 
 * for substitution.
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
 * Defining a property using substitution. This is the same example as 
 * previously but it is the properties job doing the substitution not
 * the Oddjob framework. The value of snack.favourite is escaped because we
 * want ${fruit.favourite} passed into the properties job. If the property
 * was defined in a file it would not need to be escaped like this.
 * 
 * {@oddjob.xml.resource org/oddjob/values/properties/PropertiesJobWithSubstitution.xml}
 * 
 * @oddjob.example
 * 
 * Loading properties from a class path resource.
 * 
 * {@oddjob.xml.resource org/oddjob/values/properties/PropertiesJobFromInput.xml}
 * 
 * The properties file contains:
 * 
 * {@oddjob.text.resource org/oddjob/values/properties/PropertiesJobTest1.properties}
 * 
 * This will display
 * <pre>
 * John Smith
 * </pre>
 * 
 * @oddjob.example
 * 
 * Overriding Properties. Normally setting a property is first come first
 * set. Using the override property on the properties job makes the properties 
 * defined in that job take priority.
 * 
 * {@oddjob.xml.resource org/oddjob/values/properties/PropertiesJobOverriding.xml}
 * 
 * This will display
 * <pre>
 * ${fuit.favourite} is apple
 * ${fuit.favourite} is apple
 * ${fuit.favourite} is banana
 * </pre>
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
	private static final long serialVersionUID = 2014092400L;
		
	/** Delegate to properties base for implementation. (Because we
	 * can't inherit from it as we do with PropertiesType). */
	private transient PropertiesBase delegate;

	private transient ArooaSession session;
	
	private volatile transient PropertyLookup lookup;
	
	/** Prefix environment variables. */
	private volatile String environment;
	
	/** Flag to indicate setting of system variables. */
	private volatile boolean system;
	
	private volatile boolean override;
	
	private transient volatile Strategy strategy;
	
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
	protected void createPropertyLookup() {
		super.createPropertyLookup();
		if (environment != null) {
			lookup = new CompositeLookup(
					new EnvVarPropertyLookup(environment), 
					super.getLookup());
		}
	}
	
	@Override
	protected PropertyLookup getLookup() {
		if (lookup == null) {
			return super.getLookup();
		}
		else {
			return lookup;
		}		
	}
	
	@Override
	protected int execute() throws IOException, ArooaConversionException {

		setProperties(delegate.toProperties());

		if (system) {
			strategy = new SystemStrategy();
		}
		else {
			strategy = new OddjobStrategy();
		}
		
		strategy.set();
		
		return 0;
	}

	
	@Override
	public Map<String, String> describe() {
		Properties properties = getProperties();
		
		Map<String, String> description = new TreeMap<String, String>();
		
		if (properties == null) {
			return description;
		}
		
		PropertyLookup managers = session.getPropertyManager();
		
		Set<String> names = properties.stringPropertyNames();
		if (names.isEmpty()) {
			for (String name : managers.propertyNames()) {
				String value = managers.lookup(name);
				PropertySource source = managers.sourceFor(name);
				value += " [" + source + "]";
				description.put(name, value);
			}
		}
		else {
			Strategy strategy = this.strategy;
			if (strategy != null) {
				strategy.describe(names, description);
			}
		}
		
		return description;
	}
	
	@Override
	protected void onReset() {
		if (strategy != null) {
			strategy.unset();
			strategy = null;
		}
	}
	
	interface Strategy {
		
		void set();
		
		void unset();
		
		void describe(Set<String> names, Map<String, String> description);
	}
	
	class OddjobStrategy implements Strategy {
		
		@Override
		public void set() {
			// All the work's been done during configuration. This is all
			// we have left to do.		
			addPropertyLookup();
		}
		
		@Override
		public void unset() {
			PropertiesJob.super.onReset();
			lookup = null;
		}
		
		@Override
		public void describe(Set<String> names, Map<String, String> description) {
			
			PropertyLookup lookup = getLookup();
			if (lookup == null) {
				return;
			}
			
			PropertyLookup managers = session.getPropertyManager();
			
			for (String name : names) {
				String value = lookup.lookup(name);
				PropertySource local = lookup.sourceFor(name);
				
				PropertySource actual = managers.sourceFor(name);
				if (local != null && !local.equals(actual)) {
					value += " *(" + managers.lookup(name) +
							") [" + actual + "]";
				}
				description.put(name, value);
			}
		}
	}
	
	class SystemStrategy implements Strategy {
		
		private volatile SystemPropertyStack.Token token;
		
		@Override
		public void set() {
			if (token != null) {
				throw new IllegalStateException();
			}
			token = SystemPropertyStack.addProperties(getProperties());
		}
		
		@Override
		public void unset() {
			if (token == null) {
				throw new IllegalStateException();
			}
			SystemPropertyStack.removeProperties(token);
		}
		
		@Override
		public void describe(Set<String> names, Map<String, String> description) {
			
			PropertyLookup managers = session.getPropertyManager();
			
			for (String name : names) {
				String value = System.getProperty(name);
				PropertySource local = PropertyLookup.SYSTEM_PROPERTY_SOURCE;
				
				PropertySource actual = managers.sourceFor(name);
				if (local != null && !local.equals(actual)) {
					value += " *(" + managers.lookup(name) +
							") [" + actual + "]";
				}
				description.put(name, value);
			}
		}
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
	 * Getter for system flag.
	 * 
	 * @return The system flag.
	 */
	public boolean isSystem() {
		return system;
	}

    /**
     * @oddjob.property system
     * @oddjob.description Set to true to set System properties rather than
     * Oddjob properties.
     * @oddjob.required No. Defaults to false
     */
	public void setSystem(boolean system) {
		this.system = system;
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
	
	private class CompositeLookup implements PropertyLookup {

		private final PropertySource propertySource = new PropertySource() {
			public String toString() {
				return PropertiesJob.this.toString();
			}
		};
		
		private final PropertyLookup environment;
		private final PropertyLookup loaded;

		CompositeLookup(PropertyLookup first, PropertyLookup second) {
			if (first == null) {
				throw new NullPointerException("First PropertyLookup null.");
			}
			if (second == null) {
				throw new NullPointerException("Second PropertyLookup null.");
			}
			this.environment = first;
			this.loaded = second;
		}
		
		@Override
		public String lookup(String propertyName) {
			String value = null;
			value = environment.lookup(propertyName);
			if (value == null) {
				value = loaded.lookup(propertyName);
			}
			return value;
		}
		
		@Override
		public Set<String> propertyNames() {
			Set<String> names = new TreeSet<String>();
			names.addAll(environment.propertyNames());
			names.addAll(loaded.propertyNames());
			return names;
		}
		
		@Override
		public PropertySource sourceFor(String propertyName) {
			if (lookup(propertyName) != null) {
				return propertySource;
			}
			return null;
		}
	}
	
}
