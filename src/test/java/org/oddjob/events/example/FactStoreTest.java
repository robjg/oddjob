package org.oddjob.events.example;

import org.junit.Test;
import org.oddjob.OurDirs;
import org.oddjob.events.InstantEvent;
import org.oddjob.events.state.EventState;
import org.oddjob.tools.StateSteps;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FactStoreTest {

    @Test
    public void testSubscribeToFileFactStore() throws Exception {

        Path workDir = OurDirs.workPathDir(getClass().getSimpleName(), true);
        Files.createDirectory(workDir.resolve("BookList"));

        Path data = new File(Objects.requireNonNull(
                getClass().getResource("PricingTriggerExample.xml")).getFile())
                .getParentFile().toPath().resolve("data");

        FileFactStore fileFactStore = new FileFactStore();
        fileFactStore.setRootDir(workDir);

        fileFactStore.start();

        FactSubscriber<BookList> bookListSubscriber = new FactSubscriber<>();
        bookListSubscriber.setQuery("BookList:GREENGROCERS");
        bookListSubscriber.setFactStore(fileFactStore);

        List<InstantEvent<BookList>> results = new ArrayList<>();

        StateSteps subscriberState = new StateSteps(bookListSubscriber);
        subscriberState.startCheck(EventState.READY, EventState.CONNECTING, EventState.WAITING);

        bookListSubscriber.setTo(results::add);
        bookListSubscriber.run();

        subscriberState.checkNow();
        assertThat(results.size(), is(0));

        subscriberState.startCheck(EventState.WAITING, EventState.FIRING, EventState.TRIGGERED);

        AtomicCopy atomicCopy = new AtomicCopy();
        atomicCopy.setFrom(data.resolve("BookList/GREENGROCERS.json"));
        atomicCopy.setTo(workDir.resolve("BookList"));
        atomicCopy.run();

        subscriberState.checkWait();

        assertThat(results.size(), is(1));

        bookListSubscriber.stop();

        fileFactStore.stop();
    }
}
