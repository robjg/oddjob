package org.oddjob.persist;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.oddjob.OddjobTestHelper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.OurDirs;
import org.oddjob.Stateful;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;

public class FileSilhouettesTest extends TestCase {

	private static final Logger logger = 
		Logger.getLogger(FileSilhouettesTest.class);
	
	File archiveDir;
	
	@Override
	protected void setUp() throws Exception {
		logger.debug("------------------------- " + getName() + " ---------------------");
		
		OurDirs dirs = new OurDirs();
				
		archiveDir = new File(dirs.base(), "work/archiver");
		
		if (archiveDir.exists()) {
			FileUtils.forceDelete(archiveDir);
			logger.debug("Deleted " + archiveDir);
		}
		FileUtils.forceMkdir(archiveDir);
		logger.debug("Created " + archiveDir);
	}
	
	public static class SessionCapture implements ArooaSessionAware {

		ArooaSession arooaSession;
		
		@Override
		public void setArooaSession(ArooaSession session) {
			this.arooaSession = session;
		}
		
		public ArooaSession getArooaSession() {
			return arooaSession;
		}		
	}
	
	public void testArchiveAndRestore() throws ArooaPropertyException, ArooaConversionException, ComponentPersistException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/persist/FileSilhouetteArchiveTest1.xml",
				getClass().getClassLoader()));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());

		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		ArooaSession session = lookup.lookup("capture.arooaSession", 
				ArooaSession.class);
		
		FilePersister test = new FilePersister();
		test.setDir(archiveDir);
		
		ComponentPersister persister = test.persisterFor(null);
		
		Object silhouette = new SilhouetteFactory().create(
				lookup.lookup("seq"), session);
		
		persister.persist("one", silhouette, session);
		
		oddjob.destroy();
		
		assertTrue(new File(archiveDir, "one.ser").exists());
		
		ArooaSession session2 = new OddjobSessionFactory(
				).createSession();
		
		Object[] archives = persister.list();
		assertEquals(1, archives.length);
		assertEquals("one", archives[0]);
		
		Object restored = persister.restore("one", 
				getClass().getClassLoader(), session2);
		
		assertNotNull(restored);
		
		assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(restored));
		
		Object[] children = OddjobTestHelper.getChildren((Structural) restored);
		
		assertEquals(3, children.length);
		
		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(children[0]));
		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(children[1]));
	}
	
	public void testWithNestedArchives() throws ArooaPropertyException, ArooaConversionException, ComponentPersistException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setArgs(new String[] {
				"org/oddjob/persist/FileSilhouetteArchiveTest2-1.xml",
				"org/oddjob/persist/FileSilhouetteArchiveTest2-2.xml",
		});
		
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/persist/FileSilhouetteArchiveTest2.xml",
				getClass().getClassLoader()));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());

		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		ArooaSession session = lookup.lookup("capture.arooaSession", 
				ArooaSession.class);
		
		FilePersister test = new FilePersister();
		test.setDir(archiveDir);
		
		ComponentPersister persister = test.persisterFor(null);
		
		Object silhouette = new SilhouetteFactory().create(
				lookup.lookup("seq"), session);
		
		persister.persist("one", silhouette, session);
		
		oddjob.destroy();
				
		assertTrue(new File(archiveDir, "one.ser").exists());
		
		ArooaSession session2 = new OddjobSessionFactory(
		).createSession();

		Object[] archives = persister.list();
		assertEquals(1, archives.length);
		assertEquals("one", archives[0]);
		
		Object restored = persister.restore("one", 
				getClass().getClassLoader(), session2);
		
		assertNotNull(restored);
		
		assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(restored));
		
		Object[] children = OddjobTestHelper.getChildren((Structural) restored);
		
		assertEquals(3, children.length);
		
		Stateful hello = (Stateful) children[0];
		Stateful world= (Stateful) children[1];
		
		assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(hello));
		assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(world));
		
	}
}
