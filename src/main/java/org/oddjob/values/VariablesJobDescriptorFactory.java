package org.oddjob.values;

import org.oddjob.arooa.*;
import org.oddjob.arooa.beandocs.MappingsContents;
import org.oddjob.arooa.beanutils.DynaArooaClass;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.oddjob.arooa.deploy.MappingsSwitch;
import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.*;
import org.oddjob.designer.components.VariablesDC;

import java.net.URI;

/**
 * A {@link ArooaDescriptorFactory} for {@link VariablesJob}. This is 
 * required because the Variables Job needs it's own {@link ArooaClass}
 * to be able to set the properties correctly.
 * 
 * @author rob
 *
 */
public class VariablesJobDescriptorFactory implements ArooaDescriptorFactory {
	
	public static final ArooaElement VARIABLES = new ArooaElement("variables");
	
	static {
		ArooaClasses.register(VariablesJob.class, 
				new ArooaClassFactory<VariablesJob>() {
						
			@Override
			public ArooaClass classFor(VariablesJob instance) {
				
				return new VariablesArooaClass(instance);
			}
		});		
	}
	
	@Override
	public ArooaDescriptor createDescriptor(ClassLoader classLoader) {
		return new ArooaDescriptor() {
			
			@Override
			public ArooaBeanDescriptor getBeanDescriptor(ArooaClass forClass, 
					PropertyAccessor accessor) {
				return null;
			}
			
			@Override
			public String getPrefixFor(URI namespace) {
				return null;
			}

			@Override
			public String[] getPrefixes() {
				return new String[0];
			}

			@Override
			public URI getUriFor(String prefix) {
				return null;
			}

			@Override
			public ElementMappings getElementMappings() {
				return new MappingsSwitch( 
					new ElementMappings() {
						
						@Override
						public ArooaClass mappingFor(ArooaElement element,
								InstantiationContext parentContext) {
							if (VARIABLES.equals(element)) {
								return new VariablesArooaClass(
										new VariablesJob());
							}
							else {
								return null;
							}
						}
						
						@Override
						public ArooaElement[] elementsFor(InstantiationContext propertyContext) {
							// We should really check class here.
							return new ArooaElement[] { VARIABLES };
						}
						
						@Override
						public DesignFactory designFor(ArooaElement element,
								InstantiationContext parentContext) {
							
							if (VARIABLES.equals(element)) {
								return new VariablesDC();
							}
							
							return null;
						}
						
						@Override
						public MappingsContents getBeanDoc(ArooaType arooaType) {
							return new MappingsContents() {
								
								@Override
								public ArooaClass documentClass(ArooaElement element) {
									if (VARIABLES.equals(element)) {
										return new VariablesArooaClass(
												new VariablesJob());
									}
									return null;
								}
								
								@Override
								public ArooaElement[] allElements() {
									return new ArooaElement[] { VARIABLES };
								}
							};
						}
						
					}, null);
			}
			
			@Override
			public ConversionProvider getConvertletProvider() {
				return null;
			}
			
			@Override
			public ClassResolver getClassResolver() {
				return null;
			}
		};
	}

	static class VariablesArooaClass extends DynaArooaClass {
		
		private final VariablesJob variablesJob;
		
		public VariablesArooaClass(VariablesJob variablesJob) {
			super(variablesJob.getDynaClass(), 
					VariablesJob.class);
			this.variablesJob = variablesJob;
		}
		
		@Override
		public Object newInstance() throws ArooaInstantiationException {
			return variablesJob;
		}
	}	
}
