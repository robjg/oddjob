/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.persist;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.MockComponentPool;
import org.oddjob.arooa.registry.Path;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.framework.extend.SerializableJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Rob Gordon.
 */
public class FilePersisterTest extends OjTestCase {

    private File workDir;

    @Before
    public void setUp() throws Exception {
        workDir = OurDirs.workPathDir(getClass().getSimpleName(), true)
                .toFile();
    }

    public static class OurJob extends SerializableJob {
        private static final long serialVersionUID = 2008110500;

        private String name;

        private String text;

        public void setName(String name) {
            this.name = name;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        protected int execute() throws Throwable {
            // TODO Auto-generated method stub
            return 0;
        }
    }

    /**
     * Simple test of persisting something
     *
     * @throws ComponentPersistException
     */
    @Test
    public void testPersistAndLoad() throws ComponentPersistException {
        OurJob job = new OurJob();
        job.setName("Test");
        job.setText("Hello World");
        job.run();

        StandardArooaSession session = new StandardArooaSession();

        FilePersister test = new FilePersister();
        test.setDir(workDir);

        ComponentPersister persister = test.persisterFor(null);

        persister.persist("test-persist", job, session);

        File check = new File(workDir, "test-persist.ser");
        assertTrue(check.exists());

        job = (OurJob) persister.restore("test-persist",
                getClass().getClassLoader(), session);

        assertEquals("Test", job.name);
        assertEquals("Hello World", job.text);

        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(job));

        // check we can run it again.

        ((Resettable) job).hardReset();

        assertEquals(JobState.READY, OddjobTestHelper.getJobState(job));

        job.run();

        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(job));
    }

    class OurSession extends MockArooaSession {

        @Override
        public ComponentPool getComponentPool() {
            return new MockComponentPool() {

            };
        }
    }

    @Test
    public void testFailsOnNoDirectory() {

        FilePersister test = new FilePersister();

        test.setDir(new File(workDir, "idontexist"));

        try {
            test.persist((Path) null, (String) null, (Object) null);
            fail();
        } catch (ComponentPersistException e) {
            assertTrue(e.getMessage().startsWith("No directory"));
        }
    }

    @Test
    public void testCreatesFullPath() throws ComponentPersistException {

        FilePersister test = new FilePersister();

        test.setDir(workDir);

        test.persist(new Path("a/b/c"), "x", new OurJob());

        File check = new File(workDir, "a/b/c/x.ser");

        assertTrue(check.exists());
    }

    @Test
    public void testNullDirectory() throws ComponentPersistException {
        FilePersister persister = new FilePersister();

        try {
            persister.directoryFor(new Path());
            fail("No directory should fail.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testPersistExample() throws ArooaPropertyException, ArooaConversionException, URISyntaxException {

        URL url = getClass().getClassLoader().getResource("org/oddjob/persist/FilePersisterExample.xml");

        File file = new File(url.toURI().getPath());

        Properties props = new Properties();
        props.setProperty("important.stuff", "Important Stuff!");

        Oddjob oddjob1 = new Oddjob();
        oddjob1.setFile(file);
        oddjob1.setArgs(new String[]{workDir.getAbsolutePath()});
        oddjob1.setProperties(props);
        oddjob1.run();

        assertEquals(ParentState.COMPLETE,
                oddjob1.lastStateEvent().getState());
        oddjob1.destroy();

        assertTrue(new File(workDir, "important-jobs/save-me.ser").exists());

        Oddjob oddjob2 = new Oddjob();
        oddjob2.setFile(file);
        oddjob2.setArgs(new String[]{workDir.getAbsolutePath()});
        oddjob2.load();

        OddjobLookup lookup = new OddjobLookup(oddjob2);

        Loadable loadable = lookup.lookup("important-jobs", Loadable.class);
        loadable.load();

        String text = lookup.lookup("important-jobs/save-me.text",
                String.class);

        assertEquals("Important Stuff!", text);

        oddjob2.destroy();
    }

}
