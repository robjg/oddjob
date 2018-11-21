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

        StateSteps oddjobState = new StateSteps(oddjob);
        oddjobState.startCheck(ParentState.READY);

        oddjob.load();

        oddjobState.checkNow();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Stateful task = lookup.lookup("task", Stateful.class);

        oddjobState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.STARTED);
        StateSteps taskState = new StateSteps(task);
        taskState.startCheck(JobState.READY);

        oddjob.run();

        taskState.checkNow();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.ACTIVE));

        Runnable createFile1 = lookup.lookup("createFile1", Runnable.class);

        taskState.startCheck(JobState.READY);

        createFile1.run();

        taskState.checkNow();
        Runnable createFile2 = lookup.lookup("createFile2", Runnable.class);

        taskState.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);

        createFile2.run();

        taskState.checkWait();

        oddjobState.checkWait();

        oddjob.stop();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));
    }
}