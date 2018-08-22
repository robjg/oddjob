package org.oddjob.persist;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.MockComponentPersister;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.tools.OddjobTestHelper;

public class ArchiveBrowserJobTest extends OjTestCase {
	
	public static class OurArchiver extends MockComponentPersister 
	implements OddjobPersister {
		
		@Override
		public ComponentPersister persisterFor(String id) {
			return this;
		}
		
		@Override
		public void persist(String archiveIdentifier, Object component,
				ArooaSession session) {
			throw new RuntimeException("Unexpected.");
		}
		
		@Override
		public String[] list() {
			return new String[] { "one", "two", "three" };
		}
		
		@Override
		public Object restore(String archiveIdentifier, ClassLoader loader, ArooaSession session) {
			FlagState flag = new FlagState(JobState.INCOMPLETE);
			flag.run();

			return flag;
		}
	}
	
   @Test
	public void testBrowse() {
				
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/persist/ArchiveBrowserJobTest.xml",
				getClass().getClassLoader()));
		
		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		ArchiveBrowserJob test = (ArchiveBrowserJob) lookup.lookup("browser");

		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(test));
		
		Object[] children = OddjobTestHelper.getChildren(test);
		
		assertEquals(3, children.length);
		
		Runnable child1 = (Runnable) children[0];
		
		assertEquals("one", child1.toString());
		
		assertTrue(child1 instanceof Structural);
		
		Object[] grandChildren = OddjobTestHelper.getChildren((Structural) child1);
		
		assertEquals(0, grandChildren.length);
		
		child1.run();
		
		grandChildren = OddjobTestHelper.getChildren((Structural) child1);
		
		assertEquals(1, grandChildren.length);
		
		Object flag1 = grandChildren[0];
		
		assertEquals("FlagState", flag1.toString());
	}
}
