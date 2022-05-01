package org.oddjob.io;

import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.events.InstantEvent;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileWatchServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(FileWatchServiceTest.class);

    private static final long TIMEOUT = 1000;

    @Test
    public void testSubscribe() throws IOException, InterruptedException {

        Path testPath = OurDirs.workPathDir(PathWatchEventsTest.class.getSimpleName(), true);

        FileWatchService test = new FileWatchService();

        BlockingQueue<InstantEvent<Path>> events = new LinkedBlockingQueue<>();

        Path someFile = testPath.resolve("SomeFile.txt");

        test.start();

        Restore restore = test.subscribe(someFile, events::add);

        assertThat(events.poll(), nullValue());

        // Don't want nanos.
        Instant before = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        Files.createFile(someFile);

        Instant after = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        InstantEvent<Path> event = events.poll(TIMEOUT, TimeUnit.MILLISECONDS);
        assertThat(event, notNullValue());

        assertThat(event.getOf(), is(someFile));

        Instant modified = event.getTime().truncatedTo(ChronoUnit.MILLIS);

        assertThat(before + "<=" + modified, modified.equals(before) ||
                modified.isAfter(before), is(true));
        assertThat(modified + "<=" + after, modified.equals(after) ||
                modified.isBefore(after), is(true));

        restore.close();
    }

    @Test
    public void testOddjobTwoFileEventExample() throws IOException, ArooaConversionException, FailedToStopException, InterruptedException {

        Path testPath = OurDirs.workPathDir(PathWatchEventsTest.class.getSimpleName(), true);

        Properties properties = new Properties();
        properties.setProperty("some.dir", testPath.toString());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(
                Objects.requireNonNull(getClass()
                        .getResource("FileWatchTwoFilesExample.xml")).getFile()));
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

        String text = lookup.lookup("task.text", String.class);

        assertThat(text.contains("file1"), is(true));
        assertThat(text.contains("file2"), is(true));

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));
    }

    @Test
    public void testOddjobFileWatchInBeanBusExample() throws IOException, ArooaConversionException, InterruptedException {

        Path testPath = OurDirs.workPathDir(PathWatchEventsTest.class.getSimpleName(), true);

        Properties properties = new Properties();
        properties.setProperty("some.dir", testPath.toString());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(
                Objects.requireNonNull(getClass()
                        .getResource("FileWatchBeanBusExample.xml")).getFile()));
        oddjob.setProperties(properties);

        StateSteps oddjobState = new StateSteps(oddjob);
        oddjobState.startCheck(ParentState.READY);

        oddjob.load();

        oddjobState.checkNow();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Stateful repeatJob = lookup.lookup("repeat", Stateful.class);

        oddjobState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.COMPLETE);

        StateSteps repeatState = new StateSteps(repeatJob);
        repeatState.startCheck(ParentState.READY, ParentState.EXECUTING);

        Thread thread = new Thread(oddjob);
        thread.start();

        repeatState.checkWait();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.EXECUTING));

        Runnable createFile1 = lookup.lookup("createFile1", Runnable.class);

        createFile1.run();

        Runnable createFile2 = lookup.lookup("createFile2", Runnable.class);

        repeatState.startCheck(ParentState.EXECUTING, ParentState.COMPLETE);

        createFile2.run();

        thread.join();

        repeatState.checkNow();

        oddjobState.checkNow();

        String text = lookup.lookup("vars.results", String.class);

        logger.info("Results: {}", text);

        assertThat(text, is("Test1Test2"));

        oddjob.destroy();
    }
}