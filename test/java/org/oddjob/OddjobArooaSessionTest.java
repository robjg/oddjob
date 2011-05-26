package org.oddjob;

import junit.framework.TestCase;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.ElementMappings;
import org.oddjob.arooa.MockArooaDescriptor;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.convert.DefaultConversionProvider;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.life.MockComponentPersister;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.InvalidIdException;
import org.oddjob.arooa.registry.SimpleBeanRegistry;
import org.oddjob.arooa.registry.SimpleComponentPool;
import org.oddjob.arooa.runtime.PropertyLookup;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.standard.StandardTools;
import org.oddjob.persist.OddjobPersister;

public class OddjobArooaSessionTest extends TestCase {

	private class OurDescriptor extends MockArooaDescriptor {
		
		@Override
		public ConversionProvider getConvertletProvider() {
			return new ConversionProvider() {
				public void registerWith(ConversionRegistry registry) {
					registry.register(String.class, Integer.class, 
							new Convertlet<String, Integer>() {
						public Integer convert(String from)
								throws ConvertletException {
							return new Integer(42);
						}
					});
				}
			};
		}
		@Override
		public ElementMappings getElementMappings() {
			return null;
		}
	}

	public void testConversions() 
	throws ArooaParseException, ArooaConversionException, 
	InvalidIdException {

		OddjobSessionFactory sessionFactory = new OddjobSessionFactory();
		sessionFactory.setDescriptorFactory(
				new ArooaDescriptorFactory() {
					@Override
					public ArooaDescriptor createDescriptor(
							ClassLoader classLoader) {
						return new OurDescriptor();
					}
				});
		
		ArooaSession test = sessionFactory.createSession();
		
		ArooaConverter converter = test.getTools().getArooaConverter();
		
		assertEquals(new Integer(42), converter.convert(
				"forty two", Integer.class));
		
		test.getBeanRegistry().register("apples", "A String");

		Integer i = test.getBeanRegistry().lookup("apples", Integer.class);
		
		assertEquals(new Integer(42), i);
	}
	

	
	private class OuterSession extends MockArooaSession {
		ComponentPool componentPool = new SimpleComponentPool();
		
		BeanRegistry beanRegistry = new SimpleBeanRegistry();
		
		@Override
		public ArooaDescriptor getArooaDescriptor() {
			return new MockArooaDescriptor() {
				@Override
				public ConversionProvider getConvertletProvider() {
					return new DefaultConversionProvider();
				}
				@Override
				public ElementMappings getElementMappings() {
					return null;
				}
			};
		}
		
		@Override
		public ComponentProxyResolver getComponentProxyResolver() {
			return null;
		}
		
		@Override
		public ComponentPersister getComponentPersister() {
			return null;
		}
		
		@Override
		public ArooaTools getTools() {
			return new StandardTools();
		}
		
		@Override
		public ComponentPool getComponentPool() {
			return componentPool;
		}
		
		@Override
		public BeanRegistry getBeanRegistry() {
			return beanRegistry;
		}
	}

	
	public void testNestedConversion() throws ArooaParseException, NoConversionAvailableException, ConversionFailedException {

		OuterSession session = new OuterSession();
		
		Oddjob oddjob = new Oddjob();
		
		Helper.register(oddjob, session, null);

		OddjobSessionFactory sessionFactory = new OddjobSessionFactory();
		sessionFactory.setInherit(OddjobInheritance.NONE);
		sessionFactory.setExistingSession(session);
		sessionFactory.setDescriptorFactory(
				new ArooaDescriptorFactory() {
					
					@Override
					public ArooaDescriptor createDescriptor(ClassLoader classLoader) {
						return new OurDescriptor();
					}
			});
		
		ArooaSession test = sessionFactory.createSession(oddjob);
		
		ArooaConverter converter = test.getTools().getArooaConverter();
		
		Number number = converter.convert("2", Integer.class);
		
		assertEquals(new Integer(42), number);
	}
	
	public void testNestedPropertyManager() {
		
		StandardArooaSession outerSession = new StandardArooaSession();
		
		OddjobSessionFactory test = new OddjobSessionFactory();
		test.setExistingSession(outerSession);
		// Test Default behaviour.
		test.setInherit(null);
		
		ArooaSession session = test.createSession();
		
		outerSession.getPropertyManager().addPropertyLookup(new PropertyLookup() {
			
			@Override
			public String lookup(String propertyName) {
				assertEquals("fruit", propertyName);
				return "apple";
			}
		});
		
		assertEquals("apple", session.getPropertyManager().lookup("fruit"));
	}

	private class OurPersister extends MockComponentPersister 
	implements OddjobPersister {
		
		@Override
		public ComponentPersister persisterFor(final String id) {
			return new MockComponentPersister() {
				@Override
				public String toString() {
					return "OurPersister " + id;
				}
			};
		}
	}
	
	public void testNestedComponentPersisterNoOddjobId() {
		
		StandardArooaSession outerSession = new StandardArooaSession() {
			public ComponentPersister getComponentPersister() {
				return new OurPersister();
			};
		};
		
		Oddjob oddjob = new Oddjob();
		
		OddjobSessionFactory test = new OddjobSessionFactory();
		test.setExistingSession(outerSession);
		
		ArooaSession session = test.createSession(oddjob);
		
		assertNull(session.getComponentPersister());
	}
	
	public void testNestedComponentPersister() {
		
		StandardArooaSession outerSession = new StandardArooaSession() {
			public ComponentPersister getComponentPersister() {
				return new OurPersister();
			};
		};
		
		Oddjob oddjob = new Oddjob();
		
		outerSession.getBeanRegistry().register("stuff", oddjob);
		
		OddjobSessionFactory test = new OddjobSessionFactory();
		test.setExistingSession(outerSession);
		
		ArooaSession session = test.createSession(oddjob);
		
		assertEquals("OurPersister stuff", 
				session.getComponentPersister().toString());
	}
}
