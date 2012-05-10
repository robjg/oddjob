package org.oddjob.values.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaInterceptor;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.runtime.ExpressionParser;
import org.oddjob.arooa.runtime.ParsedExpression;
import org.oddjob.arooa.runtime.PropertyLookup;
import org.oddjob.arooa.runtime.PropertySource;
import org.oddjob.arooa.utils.ListSetterHelper;

/**
 * Base class for things that load Properties.
 */
@ArooaInterceptor("org.oddjob.values.PropertiesInterceptor")
public class PropertiesBase implements ArooaContextAware {

	private static final Logger logger = Logger.getLogger(PropertiesBase.class);
	
    /** InputStream for properties. */
	private InputStream input;
	
	/** If the input is in XML formst. */
	private boolean fromXML;

	/** Properties to merge in. */
	private List<Properties> list = new ArrayList<Properties>();
	
	/** Adhoc properties. */
	private Map<String, String> values = new LinkedHashMap<String, String>();
	
	/** Substitute within property values. */
	private boolean substitute;
	
	/** Extract this prefix from the beginning of property names. */
	private String extract;
	
	/** Add this prefix to the beginning of property names. */
	private String prefix;
	
	/** The session. */
	private ArooaSession session;
	
	private PropertySource source;
	
	@Override
	public void setArooaContext(final ArooaContext context) {
		session = context.getSession();
		
		final PropertyLookup propertyLookup = new PropertyLookup() {
			
			@Override
			public String lookup(String propertyName) {
				String value = values.get(propertyName);
				if (value != null) {
					return value;
				}
				for (Properties props : list) {
					if (props == null) {
						continue;
					}
					value = props.getProperty(propertyName);
					if (value != null) {
						return value;
					}
				}
				return null;
			}
			
			@Override
			public Set<String> propertyNames() {
				Set<String> names = new TreeSet<String>();
				names.addAll(values.keySet());
				for (Properties props : list) {
					if (props == null) {
						continue;
					}
					names.addAll(props.stringPropertyNames());
				}
				return names;
			}
			
			@Override
			public PropertySource sourceFor(String propertyName) {
				if (lookup(propertyName) != null) {
					return source;
				}
				return null;
			}
		};
		
		session.getPropertyManager().addPropertyLookup(propertyLookup);
	}

	/**
	 * The main method that provides the properties.
	 * 
	 * @return Properties, might be empty but never null.
	 * 
	 * @throws IOException
	 * @throws ArooaConversionException
	 */
	protected Properties toProperties() throws IOException, ArooaConversionException {
		
		Properties props = new Properties();
		
		load(values, props);
		
		Properties[] sets = list.toArray(new Properties[list.size()]);
		for (int i = 0; i < sets.length; ++i) {
			if (sets[i] == null) {
				throw new ArooaConversionException("Index " + i + 
						" of the property sets is null.");
			}
			load(sets[i], props);
		}
			
		if (input != null) {
			load(loadInput(), props);
		}
	
		return props;
	}

	/**
	 * Load a set of Properties.
	 * 
	 * @param set The set.
	 * @param properties The properties.
	 * @throws ArooaConversionException 
	 */
	private void load(Map<?, ?> properties, Properties into) throws ArooaConversionException {
		
		for (Map.Entry<?, ?> entry: properties.entrySet()) {
			
			String name = entry.getKey().toString();
			String value = entry.getValue().toString(); 
			
			if (extract != null) {
				String extractWithDot = extract + ".";
				
				if (!name.startsWith(extractWithDot)) {
					continue;
				}
				
				name = name.substring(extractWithDot.length());
			}
		
			if (prefix != null) {
				name = prefix + "." + name;
			}
			
			if (substitute) {
				ExpressionParser parser = session.getTools().getExpressionParser();
				ParsedExpression expression = parser.parse(value);
				value = expression.evaluate(session, String.class);
			}
			
			if (value == null) {
				logger.info(name +" is null. skipping.");
				continue;
			}
			
			into.setProperty(name, value);
		}	
	}
		
	/**
	 * Utility method to load properties from an InputStream.
	 * 
	 * @return The properties, never null.
	 * 
	 * @throws IOException
	 */
	private Properties loadInput() throws IOException {
		
		Properties props = new Properties();
		
		try {
			if (fromXML) {
				props.loadFromXML(input);
			}
			else {
				props.load(input);
			}
			
			return props;
		}
		finally {
			input.close();
		}
	}
	
    /**
     * @oddjob.property input
     * @oddjob.description An input source for Properties.
     * @oddjob.required No.
     */
	public void setInput(InputStream input) {
		this.input = input;
	}
	
    /**
     * @oddjob.property fromXML
     * @oddjob.description If the input for the properties is in XML format.
     * @oddjob.required No, defaults to false.
     */
	public void setFromXML(boolean fromXml) {
		this.fromXML = fromXml;
	}
	
	/**
	 * Getter for fromXML.
	 * 
	 * @return true/false.
	 */
	public boolean isFromXML() {
		return fromXML;
	}
		
    /**
     * @oddjob.property values
     * @oddjob.description Properties defined as key value pairs.
     * @oddjob.required No.
     */
	public void setValues(String key, String value) {
		if (value == null) {
			values.remove(key);
		}
		else {
			values.put(key, value);
		}		
	}

    /**
     * @oddjob.property sets
     * @oddjob.description Extra properties to be merged into the overall
     * property set.
     * @oddjob.required No.
     */
	public void setSets(int index, Properties props) {
		new ListSetterHelper<Properties>(list).set(index, props);
	}
	
	/**
	 * Indexed getter for sets.
	 * 
	 * @param index The index.
	 * @return The properites.
	 */
	public Properties getSets(int index) {
		return list.get(index);
	}
	
    /**
     * @oddjob.property substitute
     * @oddjob.description Use substitution for the values of ${} type 
     * properties.
     * @oddjob.required No.
     */
	public void setSubstitute(boolean substitute) {
		this.substitute = substitute;
	}

	/**
	 * Getter for substitute.
	 * 
	 * @return true/false.
	 */
	public boolean isSubstitute() {
		return substitute;
	}

	/**
	 * Getter for extract.
	 * 
	 * @return The extract prefix or null.
	 */
	public String getExtract() {
		return extract;
	}

    /**
     * @oddjob.property extract
     * @oddjob.description Extract this prefix form property names. Filters
     * out properties that do not begin with this prefix.
     * @oddjob.required No.
     */
	public void setExtract(String extract) {
		this.extract = extract;
	}

	/**
	 * Getter for prefix.
	 * 
	 * @return The appending prefix or null.
	 */
	public String getPrefix() {
		return prefix;
	}

    /**
     * @oddjob.property prefix
     * @oddjob.description Append this prefix to property names.
     * @oddjob.required No.
     */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public PropertySource getSource() {
		return source;
	}

	public void setSource(PropertySource source) {
		this.source = source;
	}
	
}
