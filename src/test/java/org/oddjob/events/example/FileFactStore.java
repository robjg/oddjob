package org.oddjob.events.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.events.EventOf;
import org.oddjob.io.FileWatch;
import org.oddjob.io.FileWatchService;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Fact store backed by the file system.
 */
public class FileFactStore implements FactStore {

    private static final Logger logger = LoggerFactory.getLogger(FileFactStore.class);

    private volatile String name;

    private volatile Path rootDir;

    private volatile Service service;

    public void start() {

        if (service != null) {
            throw new IllegalStateException("Started already.");
        }

        Path rootDir = Optional.ofNullable(this.rootDir)
                .orElseThrow(() -> new IllegalArgumentException("No Root Path"));

        service = new Service(rootDir);
    }

    public void stop() {

        if (service == null) {
            throw new IllegalStateException("Not Started.");
        }
        service.close();
        service = null;
    }


    @Override
    public <T> Restore subscribe(String query, Consumer<? super EventOf<T>> consumer) throws ClassNotFoundException {
        return Optional.ofNullable(service).orElseThrow(() -> new IllegalStateException("Not Started"))
                .subscribe(query, consumer);
    }

    static class Service implements FactStore {

        private final Path rootDir;

        private final FileWatchService fileWatchService;

        private Map<Consumer<?>, Instant> lastModified = new ConcurrentHashMap<>();

        Service(Path rootDir) {
            this.rootDir = rootDir;

            this.fileWatchService = new FileWatchService();
            this.fileWatchService.setKinds(StandardWatchEventKinds.ENTRY_CREATE.name());
            this.fileWatchService.start();
        }

        @Override
        public <T> Restore subscribe(String query, Consumer<? super EventOf<T>> consumer) throws ClassNotFoundException {

            Query parsedQuery = parseQuery(query);

            Path fullPath = buildFullPath(rootDir, parsedQuery);

            logger.debug("Consumer [" + consumer + "] subscribing to " + fullPath);

            Restore fileWatchRestore = fileWatchService.subscribe(fullPath, p -> {
                logger.debug("Received path {} with time {}", p.getOf(), p.getTime());
                if (p.getTime().equals(this.lastModified.put(
                        consumer, p.getTime()))) {
                    logger.debug("Last modified the same. Ignoring");
                    return;
                }
                T fact = (T) readFact(p.getOf(), parsedQuery.type);
                Optional.ofNullable(fact)
                        .ifPresent(f -> consumer.accept(EventOf.of(f, p.getTime())));
            });

            return () -> {
                this.lastModified.remove(consumer);
                fileWatchRestore.close();
            };
        }

        void close() {
            fileWatchService.stop();
        }

        int getNumberOfConsumers() {
            return this.fileWatchService.getNumberOfConsumers();
        }
    }

    static <T> T readFact(Path path, Class<T> clazz) {

        ObjectMapper mapper = new ObjectMapper();

        while (true) {
            try {
                return mapper.readValue(path.toFile(),
                        clazz);
            } catch (IOException e) {
                if (e.getMessage().contains(
                        "cannot access the file because it is being used by another process")) {
                    logger.warn("{} being used by another process, retrying...", path);
                    try {
                        Thread.sleep(100L);
                    }
                    catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                else {
                    logger.error("Failed reading " + path, e);
                }
                throw new RuntimeException(e);
            }
        }
    }

    static Path buildFullPath(Path rootDir, Query query) {

        return rootDir.resolve(query.getType().getSimpleName())
                .resolve(query.getKey() + ".json");
    }

    static Query parseQuery(String query) throws ClassNotFoundException {

        String[] parts = query.split(":");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected query of form <type>:<key>");
        }

        String type = parts[0];
        String key = parts[1];

        String fullClass = FileFactStore.class.getPackage().getName() + "." + type;

        Class<?> factClass = Class.forName(fullClass);

        return new Query(factClass, key);
    }

    static class Query {

        private final Class<?> type;

        private final String key;

        Query(Class<?> type, String key) {
            this.type = type;
            this.key = key;
        }

        public Class<?> getType() {
            return type;
        }

        public String getKey() {
            return key;
        }
    }

    public int getNumberOfConsumers() {
        return Optional.ofNullable(service)
                .map(Service::getNumberOfConsumers)
                .orElse(0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Path getRootDir() {
        return rootDir;
    }

    @ArooaAttribute
    public void setRootDir(Path rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public String toString() {
        return Optional.ofNullable(name).orElseGet(
                () -> getClass().getSimpleName());
    }
}
