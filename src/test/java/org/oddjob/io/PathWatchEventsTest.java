package org.oddjob.io;

import org.junit.Test;
import org.oddjob.OurDirs;
import org.oddjob.events.EventOf;
import org.oddjob.util.Restore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class PathWatchEventsTest {

    private static final long TIMEOUT = 1000;

    @Test
    public void testFileCreateModify() throws IOException, InterruptedException {

        Path testPath = OurDirs.workPathDir(PathWatchEventsTest.class.getSimpleName(), true);

        Path test0 = Files.createFile(testPath.resolve("test0.txt"));

        PathWatchEvents test = new PathWatchEvents();
        test.setDir(testPath);
        BlockingQueue<EventOf<Path>> paths = new LinkedBlockingQueue<>();

        Restore restore = test.doStart(paths::add);

        assertThat(paths.poll(TIMEOUT, TimeUnit.MILLISECONDS).getOf(),
                is( test0));

        Path test1 = Files.createFile(testPath.resolve("test1.txt"));

        assertThat(paths.poll(TIMEOUT, TimeUnit.MILLISECONDS).getOf(),
                is( test1));

        Path test2 = Files.createFile(testPath.resolve("test2.txt"));

        assertThat(paths.poll(TIMEOUT, TimeUnit.MILLISECONDS).getOf(),
                is( test2));

        Files.setLastModifiedTime(test1, FileTime.from(Instant.now()));

        assertThat(paths.poll(TIMEOUT, TimeUnit.MILLISECONDS).getOf(),
                is( test1));

        assertThat(paths.poll(), nullValue());

        restore.close();
    }

    @Test
    public void testFileCreateWhileConsuming() throws IOException, InterruptedException {

        Path testPath = OurDirs.workPathDir(PathWatchEventsTest.class.getSimpleName(), true);

        Path test0 = Files.createFile(testPath.resolve("test0.txt"));

        BlockingQueue<EventOf<Path>> paths = new LinkedBlockingQueue<>();

        AtomicReference<Path> test1 = new AtomicReference<>();

        Consumer<EventOf<Path>> c = eventOf -> {
            Path path = eventOf.getOf();
            if (test0.equals(path)) {
                try {
                    test1.set(Files.createFile(testPath.resolve("test1.txt")));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                paths.add(eventOf);
            }
        };

        PathWatchEvents test = new PathWatchEvents();
        test.setDir(testPath);

        Restore restore = test.doStart(c);

        assertThat(paths.poll(TIMEOUT, TimeUnit.MILLISECONDS).getOf(),
                is( test1.get()));

        assertThat(paths.poll(), nullValue());

        restore.close();
    }

    @Test
    public void testFileCreateLargeFile() throws IOException, InterruptedException {

        Path testPath = OurDirs.workPathDir(PathWatchEventsTest.class.getSimpleName(), true);

        PathWatchEvents test = new PathWatchEvents();
        test.setDir(testPath);
        BlockingQueue<EventOf<Path>> paths = new LinkedBlockingQueue<>();

        Restore restore = test.doStart(paths::add);

        Path test1 = testPath.resolve("test1.txt");

        byte[ ] bytes = new byte[1024];
        Arrays.fill(bytes, (byte) 'x');
        Files.write(test1, bytes);

        Set<Instant> modifiedTime = new HashSet<>();

        while(true) {
            EventOf<Path> next = paths.poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if (next == null) {
                break;
            }
            modifiedTime.add(next.getTime());
        }

        assertThat( "We could get different times." + modifiedTime,
                modifiedTime.size() > 0, is(true ));

        restore.close();
    }


}