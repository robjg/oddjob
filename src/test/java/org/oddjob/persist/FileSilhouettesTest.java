package org.oddjob.persist;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FileSilhouettesTest extends OjTestCase {

    private static final Logger logger =
            LoggerFactory.getLogger(FileSilhouettesTest.class);

    private File archiveDir;

    @Before
    public void setUp() throws Exception {
        logger.debug("------------------------- " + getName() + " ---------------------");

        archiveDir = OurDirs.workPathDir(getClass().getSimpleName(), true)
                .toFile();
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

    @Test
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

        Object[] children = OddjobTestHelper.getChildren(restored);

        assertEquals(3, children.length);

        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(children[0]));
        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(children[1]));
    }

    @Test
    public void testWithNestedArchives() throws ArooaPropertyException, ArooaConversionException, ComponentPersistException {

        Oddjob oddjob = new Oddjob();
        oddjob.setArgs(new String[]{
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

        Object[] children = OddjobTestHelper.getChildren(restored);

        assertEquals(3, children.length);

        Stateful hello = (Stateful) children[0];
        Stateful world = (Stateful) children[1];

        assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(hello));
        assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(world));

    }
}
