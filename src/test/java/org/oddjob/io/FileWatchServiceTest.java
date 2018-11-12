package org.oddjob.io;

import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FileWatchServiceTest {

    @Test
    public void testOddjobExample() throws IOException, ArooaConversionException, FailedToStopException, InterruptedException {

        Path testPath = OurDirs.workPathDir(PathWatchEventsTest.class.getSimpleName(), true);

        Properties properties = new Properties();
        properties.setProperty("some.dir", testPath.toString());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(
                getClass().getResource("FileWatchTwoFilesExample.xml").getFile()));
        oddjob.setProperties(properties);

        oddjob.load();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.READY));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Stateful task = lookup.lookup("task", Stateful.class);

        StateSteps taskSteps = new StateSteps(task);
        taskSteps.startCheck(JobState.READY);

        oddjob.run();

        taskSteps.checkNow();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.ACTIVE));

        Runnable createFile1 = lookup.lookup("createFile1", Runnable.class);

        taskSteps.startCheck(JobState.READY);

        createFile1.run();

        taskSteps.checkNow();
        Runnable createFile2 = lookup.lookup("createFile2", Runnable.class);

        taskSteps.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);

        createFile2.run();

        taskSteps.checkWait();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.STARTED));

        oddjob.stop();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));
    }
}