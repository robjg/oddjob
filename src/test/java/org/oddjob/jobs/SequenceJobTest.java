/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.framework.adapt.job.JobAdaptor;
import org.oddjob.framework.adapt.job.JobProxyGenerator;
import org.oddjob.framework.adapt.job.JobStrategies;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.Matchers.is;

public class SequenceJobTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(SequenceJobTest.class);

    @Before
    public void setUp() throws Exception {
        logger.debug("------------------ " + getName() + " -----------------");
    }

    @Test
    public void testSerialize() throws Exception {
        SequenceJob test = new SequenceJob();
        test.setFrom(22);

        test.run();

        assertEquals(Integer.valueOf(22), test.getCurrent());

        SequenceJob copy = OddjobTestHelper.copy(test);

        assertEquals(Integer.valueOf(22), copy.getCurrent());
    }

    @Test
    public void testSerializedByWrapper() throws Exception {

        ArooaSession session = new StandardArooaSession();

        SequenceJob test = new SequenceJob();
        test.setFrom(22);

        Optional<JobAdaptor> jobAdaptor = new JobStrategies().adapt(test, session);
        assertThat(jobAdaptor.isPresent(), is(true));

        Runnable proxy = (Runnable) new JobProxyGenerator().generate(
                jobAdaptor.get(),
                getClass().getClassLoader());

        ((ArooaSessionAware) proxy).setArooaSession(session);

        proxy.run();

        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(proxy));

        Object copy = OddjobTestHelper.copy(proxy);

        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(copy));
    }

    @Test
    public void testDescribe() {

        SequenceJob test = new SequenceJob();

        test.run();

        Map<String, String> m = new UniversalDescriber(
                new StandardArooaSession()).describe(test);

        String current = m.get("current");
        assertEquals("0", current);
    }

    @Test
    public void testSequenceExample() throws IOException, ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {

        File workDir = OurDirs.workPathDir(getClass().getSimpleName(), true)
                .toFile();

        Properties properties = new Properties();
        properties.setProperty("work.dir", workDir.getPath());

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/SequenceInFileNameExample.xml",
                getClass().getClassLoader()));
        oddjob.setProperties(properties);

        oddjob.run();

        Date now = new Date(new Date().getTime() + 1);
        while (true) {
            Date next = new OddjobLookup(oddjob).lookup(
                    "daily.nextDue", Date.class);

            if (next.after(now)) {
                break;
            }

            logger.info("Waiting for daily to move forward.");

            //noinspection BusyWait
            Thread.sleep(100);
        }

        oddjob.stop();

        assertEquals(ParentState.READY,
                oddjob.lastStateEvent().getState());

        assertTrue(new File(workDir, "sequence0009.txt").exists());

        oddjob.destroy();
    }
}
