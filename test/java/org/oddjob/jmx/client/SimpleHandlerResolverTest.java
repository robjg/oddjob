package org.oddjob.jmx.client;

import org.apache.log4j.Logger;
import org.oddjob.arooa.life.ClassLoaderClassResolver;

import junit.framework.TestCase;

public class SimpleHandlerResolverTest extends TestCase {

	private static final Logger logger = Logger.getLogger(SimpleHandlerResolverTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
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
