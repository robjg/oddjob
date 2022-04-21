package org.oddjob.io;

import org.oddjob.beanbus.Outbound;
import org.oddjob.framework.Service;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Watch a directory for files and changes.
 *
 * @author rob
 */
public class PathWatchEvents implements Outbound<Path>, Service {

    private static final Logger logger = LoggerFactory.getLogger(PathWatchEvents.class);

    private volatile Path dir;

    private volatile String kinds;

    private volatile String filter;

    private volatile Consumer<? super Path> to;

    private volatile boolean newOnly;

    private volatile Restore close;

    @Override
    public void start() {

        try {
            this.close = doStartWithException(
                    Objects.requireNonNull(this.to, "No Destination"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void stop() {
        Optional.ofNullable(this.close).ifPresent(Restore::close);
    }

    protected Restore doStartWithException(Consumer<? super Path> consumer) throws IOException {

        final Path path = Optional.ofNullable(this.dir)
                .orElse(Paths.get("."));

        final WatchEvent.Kind<?>[] kindArray = Optional.ofNullable(this.kinds)
                .map(this::toKinds)
                .orElseGet(() -> new WatchEvent.Kind<?>[] {
                        ENTRY_CREATE, ENTRY_MODIFY });

        logger.info("Starting watch on {} for events {}",
                    path,
                    Arrays.toString(kindArray));

        final Predicate<String> filter = Optional.ofNullable(this.filter)
                .map(Pattern::compile)
                .map(Pattern::asPredicate)
                .orElse(ignore -> true);

        final Consumer<Path> filterConsumer = p -> {
            if (filter.test(p.getFileName().toString())) {
                consumer.accept(p);
            }
        };

        FileSystem fs = FileSystems.getDefault();

        WatchService watchService = fs.newWatchService();

        WatchKey regKey = path.register(watchService, kindArray);

        Thread thread = new Thread(() -> {
            while (true) {

                WatchKey key;
                try {
                    key = watchService.take();
                }
                catch (InterruptedException e) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // This key is registered only
                    // for ENTRY_CREATE events,
                    // but an OVERFLOW event can
                    // occur regardless if events
                    // are lost or discarded.
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    // The filename is the
                    // context of the event.
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    logger.debug("WatchEvent: {}, Path={} (count {})",
                            event.kind(), event.context(), event.count());
                    Path found = path.resolve(ev.context());
                    filterConsumer.accept(found);
                }

                // Reset the key -- this step is critical if you want to
                // receive further watch events.  If the key is no longer valid,
                // the directory is inaccessible so exit the loop.
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }

            }
        });
        thread.setUncaughtExceptionHandler((t, e) ->
                logger.error("Unexpected Exception in Thread {} while watching {}",
                        t.getName(), path, e));
        thread.start();

        if (!newOnly) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {

                StreamSupport.stream(directoryStream.spliterator(), false)
                        .forEach(filterConsumer);
            }
        }

        return () -> {
            regKey.cancel();
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                watchService.close();
            } catch (IOException e) {
                // ignore
            }
        };
    }

    private WatchEvent.Kind<?>[] toKinds(String kinds) {
        String[] kindStringArray = kinds.split("\\s*,\\s*");
        return Arrays.stream(kindStringArray)
                .map(s -> {
                    if (s.equals(ENTRY_CREATE.name())) {
                        return ENTRY_CREATE;
                    } else if (s.equals(ENTRY_DELETE.name())) {
                        return ENTRY_DELETE;
                    } else if (s.equals(ENTRY_MODIFY.name())) {
                        return ENTRY_MODIFY;
                    } else {
                        throw new IllegalArgumentException("No StandardWatchEventKinds " + s);
                    }
                }).toArray(WatchEvent.Kind<?>[]::new);
    }

    public Path getDir() {
        return dir;
    }

    public void setDir(Path dir) {
        this.dir = dir;
    }

    public String getKinds() {
        return kinds;
    }

    public void setKinds(String kinds) {
        this.kinds = kinds;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Consumer<? super Path> getTo() {
        return to;
    }

    @Override
    public void setTo(Consumer<? super Path> to) {
        this.to = to;
    }

    public boolean isNewOnly() {
        return newOnly;
    }

    public void setNewOnly(boolean newOnly) {
        this.newOnly = newOnly;
    }

}
