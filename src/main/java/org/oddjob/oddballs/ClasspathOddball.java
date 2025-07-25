package org.oddjob.oddballs;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.deploy.ClassPathDescriptorFactory;
import org.oddjob.arooa.deploy.ClassesOnlyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClasspathOddball implements Oddball {

    private static final Logger logger = LoggerFactory.getLogger(ClasspathOddball.class);

    private final ClassLoader classLoader;

    private final ArooaDescriptor descriptor;

    private final String description;

    private ClasspathOddball(ClassLoader classLoader,
                             ArooaDescriptor descriptor,
                             String description) {
        this.classLoader = classLoader;
        this.descriptor = descriptor;
        this.description = description;
    }

    public static Oddball from(ClassLoader parentLoader,
                               File... files) throws IOException {
        return from(parentLoader,
                Arrays.stream(files)
                        .map(File::toPath)
                        .toArray(Path[]::new));
    }

    public static Oddball from(ClassLoader parentLoader,
                               Path... files) throws IOException {

        String description = Arrays.stream(files)
                .map(Objects::toString)
                .collect(Collectors.joining(File.pathSeparator));

        return from (parentLoader, "Oddball for " + description, files);
    }

    public static Oddball from(ClassLoader parentLoader,
                               String description,
                               File... files) throws IOException {

        return from(parentLoader,
                description,
                Arrays.stream(files)
                        .map(File::toPath)
                        .toArray(Path[]::new));

    }

    public static Oddball from(ClassLoader parentLoader,
                               String description,
                               Path... paths) throws IOException {

        URL[] urls = new URL[paths.length];
        for (int i = 0; i < urls.length; ++i) {
            urls[i] = paths[i].toUri().toURL();
        }

        return from(parentLoader, description, urls);
    }

    public static Oddball from(ClassLoader parentLoader,
                               URL... urls) {

        String description = Arrays.stream(urls)
                .map(Objects::toString)
                .collect(Collectors.joining(File.pathSeparator));

        return from(parentLoader, "Oddball for " + description, urls);
    }

    public static Oddball from(ClassLoader parentLoader,
                               String description,
                               URL... urls) {

        if (urls.length == 0) {
            return null;
        }

        final URLClassLoader classLoader = new URLClassLoader(
                urls, parentLoader) {
            @Override
            public String toString() {
                return "ClassLoader for " + description;
            }
        };

        return from(classLoader, description);
    }

    public static Oddball from(ClassLoader classLoader) {
        return from(classLoader, "Oddball for " + classLoader);
    }

    public static Oddball from(ClassLoader classLoader,
                               String description) {

        ClassPathDescriptorFactory descriptorFactory =
                new ClassPathDescriptorFactory();
        descriptorFactory.setExcludeParent(true);

        ArooaDescriptor maybeDescriptor =
                descriptorFactory.createDescriptor(classLoader);

        if (maybeDescriptor == null) {
            logger.debug("No arooa.xml in Oddball. Using for classes only.");
            maybeDescriptor = new ClassesOnlyDescriptor(classLoader);
        }

        final ArooaDescriptor descriptor = maybeDescriptor;

        return new ClasspathOddball(classLoader, descriptor, description);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public ArooaDescriptor getArooaDescriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return description;
    }
}
