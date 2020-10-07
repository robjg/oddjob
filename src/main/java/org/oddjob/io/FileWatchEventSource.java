package org.oddjob.io;

import org.oddjob.events.EventOf;
import org.oddjob.events.EventSourceBase;
import org.oddjob.events.Trigger;
import org.oddjob.events.When;
import org.oddjob.util.Restore;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @oddjob.description Watch for a file and fire an event if/when it exists. A service such
 * as {@link FileWatchService} is required to do the actual watching.
 *
 * @oddjob.example
 *
 * See {@link FileWatchService}.
 *
 * @author rob
 *
 * @see FileWatch
 * @see Trigger
 * @see When
 */
public class FileWatchEventSource extends EventSourceBase<Path> {

    /**
     * @oddjob.property
     * @oddjob.description The service that provides the file watching ability.
     * @oddjob.required Yes.
     */
    private volatile FileWatch fileWatch;

    /**
     * @oddjob.property
     * @oddjob.description The full path of the file to be watched.
     * @oddjob.required Yes.
     */
    private volatile Path file;

    /**
     * @oddjob.property
     * @oddjob.description The last modified time of the file. Event where the file has the same
     * last modified time will be ignored.
     * @oddjob.required R/O.
     */
    private volatile AtomicReference<Instant> lastModified = new AtomicReference<>();

    @Override
    protected Restore doStart(Consumer<? super EventOf<Path>> consumer) throws IOException {
        Objects.requireNonNull(consumer, "No Consumer");

        Path file = Optional.ofNullable(this.file)
                .orElseThrow(() -> new IllegalArgumentException("No file."));

        FileWatch fileWatch = Optional.ofNullable(this.fileWatch)
                .orElseThrow(() -> new IllegalArgumentException("No file watch"));

        return fileWatch.subscribe(file, pathEvent -> {
            Instant lastModified = pathEvent.getTime();
            if (!lastModified.equals(this.lastModified.getAndSet(lastModified))) {
                consumer.accept(pathEvent);
            }
        });
    }


    public Path getFile() {
        return file;
    }

    public void setFile(Path file) {
        this.file = file;
    }

    public FileWatch getFileWatch() {
        return fileWatch;
    }

    public void setFileWatch(FileWatch fileWatch) {
        this.fileWatch = fileWatch;
    }

    public Instant getLastModified() {
        return this.lastModified.get();
    }
}
