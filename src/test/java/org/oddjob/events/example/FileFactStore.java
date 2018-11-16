package org.oddjob.events.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.io.FileWatch;
import org.oddjob.io.FileWatchService;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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
    public <T> Restore subscribe(String query, Consumer<? super T> consumer) throws ClassNotFoundException {
        return Optional.ofNullable(service).orElseThrow(() -> new IllegalStateException("Not Started"))
                .subscribe(query, consumer);
    }

    static class Service implements FactStore {

        private final Path rootDir;

        private final FileWatchService fileWatchService;

        private Map<Consumer<?>, FileTime> lastModified = new ConcurrentHashMap<>();

        Service(Path rootDir) {
            this.rootDir = rootDir;

            this.fileWatchService = new FileWatchService();
            this.fileWatchService.start();
        }

        @Override
        public <T> Restore subscribe(String query, Consumer<? super T> consumer) throws ClassNotFoundException {

            Query parsedQuery = parseQuery(query);

            Path fullPath = buildFullPath(rootDir, parsedQuery);

            logger.debug("Consumer " + consumer + " subscribing to " + fullPath);

            Restore fileWatchRestore = fileWatchService.subscribe(fullPath, p -> {
                try {
                    FileTime lastModified = Files.getLastModifiedTime(p);
                    if (lastModified.equals(this.lastModified.put(
                            consumer, lastModified))) {
                        return;
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                T t = (T) readFact(p, parsedQuery.type);
                consumer.accept(t);
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
            return this.lastModified.size();
        }
    }

    static <T> T readFact(Path path, Class<T> clazz) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(path.toFile(),
                    clazz);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
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
                .map( Service::getNumberOfConsumers)
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
