/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.values.properties;

import java.util.Properties;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;

/**
 * <p>
 * @oddjob.description A type that evaluates to a java Properties object.
 * <p>
 * For more information on configuring this please see {@link PropertiesJob}
 * as they share the same underlying mechanisms.
 * 
 * @oddjob.example
 * 
 * Defining a single property.
 * 
 * <pre>
 * &lt;variables id="vars"&gt;
 *   &lt;props&gt;
 *     &lt;properties&gt;
 *      &lt;values&gt;
 *       &lt;value key="snack.fruit" value="apple"/&gt;
 *      &lt;/values&gt;
 *     &lt;/properties&gt;
 *   &lt;props/&gt;
 * &lt;/variables&gt;
 * </pre>
 * 
 * @author Rob Gordon.
 * 
 */
public class PropertiesType extends PropertiesBase 
implements ArooaValue {

	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {
			registry.register(PropertiesType.class, Properties.class, 
					new Convertlet<PropertiesType, Properties>() {
				public Properties convert(PropertiesType from) throws ConvertletException {
					try {
						return from.toProperties();
					}
					catch (Exception e) {
						throw new ConvertletException(e);
					}
				}
			});
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
