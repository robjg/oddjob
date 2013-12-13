package org.oddjob.sql;

import junit.framework.TestCase;

import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.tools.OddjobTestHelper;

public class ConnectionTypeTest extends TestCase {

	public void testSerialize() throws Exception {
		ConnectionType test = new ConnectionType();
		test.setUrl("x:/y/z");
		
		ConnectionType copy = (ConnectionType) OddjobTestHelper.copy(test);
		
		assertEquals("x:/y/z", copy.getUrl());
	}
	
	public void testIsClassLoaderAuto() throws ArooaParseException {
		
		ArooaSession session = new OddjobSessionFactory().createSession();
		
		ArooaBeanDescriptor descriptor = 
			session.getArooaDescriptor().getBeanDescriptor(
					new SimpleArooaClass(ConnectionType.class),
					session.getTools().getPropertyAccessor());
		
		assertTrue(descriptor.isAuto("classLoader"));
	}
	
	public void testBadUrl() {
		
		ConnectionType test = new ConnectionType();
		test.setDriver("org.hsqldb.jdbcDriver");
		test.setUrl("jdbc.url=jdbc:hsqldb:mem:test");
		test.setUsername("sa");
		test.setPassword("");
		
		try {
			test.toValue();
			fail("Should fail.");
		} catch (ArooaConversionException e) {
			assertTrue(e.getMessage().startsWith(
					"No connection available for"));
		}
	}
	
	public void testBadUrl2() {
		
		ConnectionType test = new ConnectionType();
		test.setDriver("org.hsqldb.jdbcDriver");
		test.setUrl("jdbc.url=");
		test.setUsername("sa");
		test.setPassword("");
		
		try {
			test.toValue();
			fail("Should fail.");
		} catch (ArooaConversionException e) {
			assertTrue(e.getMessage().startsWith(
					"No connection available for"));
		}
	}
}
