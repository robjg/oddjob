/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.persist;

import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.Path;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.framework.SerializableJob;
import org.oddjob.tools.OddjobTestHelper;

public class PersisterBaseTest extends OjTestCase {

	public static class OurJob extends SerializableJob 
	implements Serializable {
		private static final long serialVersionUID = 2008110500L;
		
		@Override
		protected int execute() throws Throwable {
			return 0;
		}
	}
	
	private class OurPersister extends MockPersisterBase {
		Path path;
		String id;
		Object component;

		@Override		
		protected void persist(Path path, String id, Object component) {
			this.path = path;
			this.id = id;
			try {
				this.component = OddjobTestHelper.copy(component);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		protected Object restore(Path path, String id, ClassLoader classLoader) {
			this.path = path;
			this.id = id;
			return component;
		}
		
		@Override
		protected void remove(Path path, String id) {
			throw new RuntimeException("Unexpected.");
		}
		
	}

	
	private class OurContext extends MockArooaContext {
	
		ArooaSession session;

		OurContext(ArooaSession session) {
			this.session = session;
		}
		
		@Override
		public ArooaSession getSession() {
			return session;
		}
		
		@Override
		public RuntimeConfiguration getRuntime() {
			return new MockRuntimeConfiguration() {
				@Override
				public void configure() {
				}
			};
		}
	}
	
   @Test
	public void testInitialiseAndPersist() throws Exception {
		final OurPersister test = new OurPersister();

		ArooaSession session = new StandardArooaSession() {
			@Override
			public ComponentPersister getComponentPersister() {
				return test.persisterFor("test");
			}
		};
		
		ComponentPool components = session.getComponentPool();
		
		OurJob j = new OurJob();
		
		components.registerComponent(
				new ComponentTrinity(
					j, j, new OurContext(session)), "foo");
		
		j.setArooaSession(session);
		
		j.run();
		
		assertEquals(new Path("test"), test.path);
		assertEquals("foo", test.id);
		assertTrue(test.component instanceof OurJob);

		ComponentPersister persister = test.persisterFor("test");
		
		Object restore = persister.restore("foo", 
				getClass().getClassLoader(), session);
		
		assertEquals(new Path("test"), test.path);
		assertEquals("foo", test.id);
		
		assertTrue(restore instanceof OurJob);
	}
	
	static class OurComp implements Serializable {
		private static final long serialVersionUID = 2009042100L;
	}
		
   @Test
	public void testWithPath() throws ComponentPersistException {

		OurPersister test = new OurPersister();

		test.setPath("persistDir");
		ComponentPersister persister = test.persisterFor(null);
		
		persister.persist("y", new OurComp(), new MockArooaSession());
		
		assertEquals(new Path("persistDir"), test.path);
		test.path = null;
		
		Object result = persister.restore("y", 
				getClass().getClassLoader(), new MockArooaSession());
		
		assertEquals(new Path("persistDir"), test.path);
		
		assertEquals(test.component, result);
		
		try {
			((OddjobPersister) persister).persisterFor(null);
			fail("Should fail.");
		}
		catch (NullPointerException e) {
			// expected.
		}
		
		ComponentPersister persister2 = ((OddjobPersister) persister).persisterFor("child");
		
		persister2.persist("y", new OurComp(), new MockArooaSession());
		
		assertEquals(new Path("persistDir/child"), test.path);
		test.path = null;
		
		ComponentPersister persister3 = 
			((OddjobPersister) persister2).persisterFor("grandchild");
		
		persister3.persist("y", new OurComp(), new MockArooaSession());
		
		assertEquals(new Path("persistDir/child/grandchild"), test.path);
	}
	
}
