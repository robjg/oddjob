package org.oddjob.values;

import java.net.URI;

import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.ClassResolver;
import org.oddjob.arooa.ElementMappings;
import org.oddjob.arooa.beandocs.MappingsContents;
import org.oddjob.arooa.beanutils.DynaArooaClass;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.oddjob.arooa.deploy.MappingsSwitch;
import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.ArooaClassFactory;
import org.oddjob.arooa.reflect.ArooaClasses;
import org.oddjob.arooa.reflect.ArooaInstantiationException;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.designer.components.VariablesDC;

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
