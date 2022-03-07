package org.oddjob.io;

import org.oddjob.events.InstantEvent;
import org.oddjob.util.Restore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @oddjob.description Provide a service for subscribers to watch a file system for Files existing, being created or being modified.
 * <p/>
 * If the file is created during subscription the consumer may receive a notification for the same file twice. Once
 * the subscription has succeeded a consumer should receive every creation and modification happening to the file.
 * <p/>
 * If this service is stopped no notification is sent to consumers. Consumers must use the state of this service
 * to know that it has stopped.
 * <p/>
 * Consumers will receive creation and modification events on a different thread to the initial event if the
 * file exists.
 * <p>
 *     <em>Implementation Note:</em> This facility is still a work in progress. Requiring this service
 *     in a configuration is messy. In future releases this service should be hidden from users.
 * </p>
 * @oddjob.example
 *
 * Trigger when two files arrive.
 *
 * {@oddjob.xml.resource org/oddjob/io/FileWatchTwoFilesExample.xml}
 *
 * @author rob
 * @see FileWatchEventSource
 */
public class FileWatchService implements FileWatch {

    /**
     * @oddjob.property
     * @oddjob.description The name of this service.
     * @oddjob.required No.
     */
    private volatile String name;

    /**
     * @oddjob.property
     * @oddjob.description Kinds of events to watch, as specified by the
     * <a link="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/StandardWatchEventKinds.html">Standard Watch Event Kinds</a>,
     * Either ENTRY_CREATE or ENTRY_MODIFY. Note that ENTRY_DELETE will not work in the current implementation.
     * @oddjob.required No.
     */
    private volatile String kinds;

    /**
     * The map of subscribers.
     */
    private volatile Map<Path, FileSystemSubscriber> subscribers;

    /**
     * @oddjob.property
     * @oddjob.description Provide a regular expression filter on the directory to reduce the stream of events.
     * @oddjob.required No.
     */
    private String filter;

    public void start() {
        if (subscribers != null) {
            throw new IllegalStateException("Already Started");
        }

        subscribers = new ConcurrentHashMap<>();
    }

    public void stop() {

        Map<Path, FileSystemSubscriber> subscribers = Optional.ofNullable(this.subscribers)
                .orElseThrow(() -> new IllegalStateException("Not Started"));

        subscribers.values().forEach(FileSystemSubscriber::close);

        this.subscribers = null;
    }

    @Override
    public Restore subscribe(Path path, Consumer<? super InstantEvent<Path>> consumer) {

        Map<Path, FileSystemSubscriber> subscribers = Optional.ofNullable(this.subscribers)
                .orElseThrow(() -> new IllegalStateException("Not Started"));

        Path dir = path.getParent();
        subscribers.computeIfAbsent(dir,
                FileSystemSubscriber::new).subscribe(path, consumer);
        return () -> unsubscribe(path, consumer);
    }

    void unsubscribe(Path path, Consumer<? super InstantEvent<Path>> consumer) {
        Path dir = path.getParent();
        subscribers.computeIfPresent(dir,
                (key, sub) -> {
                    sub.unsubscribe(path, consumer);
                    if (sub.consumers.isEmpty()) {
                        sub.restore.close();
                        return null;
                    } else {
                        return sub;
                    }
                });
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    class FileSystemSubscriber {

        private final Map<Path, List<Consumer<? super InstantEvent<Path>>>> consumers =
                new ConcurrentHashMap<>();

        private final Restore restore;

        FileSystemSubscriber(Path dir) {

            PathWatchEvents watch = new PathWatchEvents();
            watch.setDir(dir);
            watch.setKinds(kinds);
            watch.setFilter(filter);

            try {
                restore = watch.doStart(eventOf ->
                        Optional.ofNullable(consumers.get(eventOf.getOf()))
                                .ifPresent(list -> list.forEach(c -> c.accept(eventOf))));
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        void subscribe(Path path, Consumer<? super InstantEvent<Path>> consumer) {

            consumers.computeIfAbsent(path,
                    key -> new CopyOnWriteArrayList<>()).add(consumer);

            if (Files.exists(path)) {
                consumer.accept(InstantEvent.of(path, PathWatchEvents.lastModifiedOf(path)));
            }
        }

        void unsubscribe(Path path, Consumer<? super InstantEvent<Path>> consumer) {

            Optional.ofNullable(consumers.get(path)).ifPresent(list -> list.remove(consumer));
            consumers.computeIfPresent(path,
                    (p, list) -> list.size() == 0 ? null : list);
        }

        void close() {
            restore.close();
        }

        int getNumberOfConsumers() {
            return consumers.values().stream().mapToInt(List::size).sum();
        }
    }

    public int getNumberOfConsumers() {
        return Optional.ofNullable(subscribers)
                .map(s -> s.values().stream().mapToInt(FileSystemSubscriber::getNumberOfConsumers).sum())
                .orElse(0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKinds() {
        return kinds;
    }

    public void setKinds(String kinds) {
        this.kinds = kinds;
    }

    @Override
    public String toString() {
        return Optional.ofNullable(name).orElseGet(() -> getClass().getSimpleName());
    }
}
