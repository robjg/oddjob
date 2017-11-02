package org.oddjob.jmx.client;
import org.junit.Before;

import org.junit.Test;

import org.apache.log4j.Logger;
import org.oddjob.arooa.life.ClassLoaderClassResolver;

import org.oddjob.OjTestCase;

public class SimpleHandlerResolverTest extends OjTestCase {

	private static final Logger logger = Logger.getLogger(SimpleHandlerResolverTest.class);
	
   @Before
   public void setUp() throws Exception {

		logger.info("------------------  " + getName() + "  -----------------");
	}
	
	public static class MyHandlerFactory 
	implements ClientInterfaceHandlerFactory<Object> {
		@Override
		public Object createClientHandler(Object proxy,
				ClientSideToolkit toolkit) {
			throw new RuntimeException("Unexpected");
		}
		@Override
		public HandlerVersion getVersion() {
			return new HandlerVersion(1, 2);
		}
		@Override
		public Class<Object> interfaceClass() {
			return Object.class;
		}
	}
	
   @Test
	public void testResolveForMinorVersionDiferences() {
		
		SimpleHandlerResolver<Object> test = 
				new SimpleHandlerResolver<Object>(
						MyHandlerFactory.class.getName(),
						new HandlerVersion(1, 0));
		
		ClientInterfaceHandlerFactory<Object> result = 
				test.resolve(new ClassLoaderClassResolver(
						getClass().getClassLoader()));
		
		assertNotNull(result);
	}
		
	
   @Test
	public void testResolveNullForMajorVersionDiferences() {
		
		SimpleHandlerResolver<Object> test = 
				new SimpleHandlerResolver<Object>(
						MyHandlerFactory.class.getName(),
						new HandlerVersion(2, 0));
		
		ClientInterfaceHandlerFactory<Object> result = 
		test.resolve(new ClassLoaderClassResolver(
				getClass().getClassLoader()));
		
		assertNull(result);
	}
}
