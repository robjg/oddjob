/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.persist;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.structural.ChildHelper;
import org.oddjob.tools.OddjobTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.Properties;

/**
 * @author Rob Gordon.
 */
public class OddjobPersistTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(OddjobPersistTest.class);

    private File config;
    private File persistIn;

    @Before
    public void setUp() throws Exception {
        logger.debug("-------------  " + getName() + "  -------------------");

        config = OurDirs.relativePath("test/conf/oddjob-persist-test1.xml")
                .toFile();

        persistIn = OurDirs.workPathDir(getClass().getSimpleName(), true)
                .toFile();
    }

    /*
     * Test saving a job.
     */
    @Test
    public void test1Save() throws Exception {
        // delete existing file.
        FilePersister persister = new FilePersister();
        persister.setDir(persistIn);
        persister.setPath("oj2");
        ComponentPersister componentPersister = persister.persisterFor(null);
        componentPersister.clear();

        Properties props = new Properties();
        props.setProperty("some.dir", persistIn.toString());

        Oddjob oj = new Oddjob();
        oj.setName("Oddjob");
        oj.setFile(config);
        oj.setProperties(props);

        oj.run();

        assertEquals("State should be complete",
                ParentState.COMPLETE, OddjobTestHelper.getJobState(oj));

        Object seqJob = new OddjobLookup(oj).lookup("oj2/sequence");

        assertTrue(seqJob instanceof Serializable);
        assertEquals(JobState.COMPLETE,
                OddjobTestHelper.getJobState(seqJob));

        Integer current = new OddjobLookup(oj).lookup(
                "oj2/sequence.current", Integer.class);

        assertEquals(new Integer(1), current);

        sanityLoad();
    }

    /**
     * Quickly load with persister only.
     *
     * @throws Exception
     */
    public void sanityLoad() throws Exception {

        StandardArooaSession session = new StandardArooaSession();

        FilePersister test = new FilePersister();
        test.setDir(persistIn);
        test.setPath("oj2");

        ComponentPersister persister = test.persisterFor(null);

        Object seqJob = persister.restore("sequence",
                getClass().getClassLoader(), session);

        assertEquals("Persisted state should be complete", JobState.COMPLETE,
                OddjobTestHelper.getJobState(seqJob));

    }

    /**
     * Test loading the job we've just saved.
     */
    @Test
    public void test2Load() throws Exception {

        test1Save();

        Properties props = new Properties();
        props.setProperty("some.dir", persistIn.toString());

        Oddjob oj = new Oddjob();
        oj.setName("OJ again");
        oj.setFile(config);
        oj.setProperties(props);

        oj.load();

        Oddjob oj2 = (Oddjob) new OddjobLookup(oj).lookup("oj2");
        oj2.load();

        Object seqJob = new OddjobLookup(oj).lookup("oj2/sequence");

        assertNotNull(seqJob);
        assertEquals("Persisted state should be complete", JobState.COMPLETE,
                OddjobTestHelper.getJobState(seqJob));

        assertEquals("Just loaded sequence wrong.", new Integer(1),
                ((DynaBean) seqJob).get("current"));

        Object[] children = ChildHelper.getChildren(oj);
        assertEquals(1, children.length);

        // otherwise it wouldn't run.
        ((Resettable) seqJob).hardReset();

        assertEquals(JobState.READY, OddjobTestHelper.getJobState(seqJob));
        assertEquals(ParentState.READY, oj.lastStateEvent().getState());

        ((Runnable) seqJob).run();

        assertEquals("Second execute sequence wrong.", new Integer(2),
                PropertyUtils.getProperty(seqJob, "current"));
        assertEquals("State should be complete", JobState.COMPLETE,
                OddjobTestHelper.getJobState(seqJob));
    }
}
