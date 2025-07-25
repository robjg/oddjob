package org.oddjob.oddballs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * An implementation of an {@link OddballFactory} that creates an
 * {@link Oddball} from a directory.
 * <p>
 * If the given file is not a directory no Oddball is created.
 *
 * @author rob
 */
public class DirectoryOddballFactory implements OddballFactory {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryOddballFactory.class);

    private final File directory;

    private DirectoryOddballFactory(File directory) {
        this.directory = directory;
    }

    public static OddballFactory from(File directory) {
        return new DirectoryOddballFactory(directory);
    }

    public static OddballFactory from(Path directory) {
        return new DirectoryOddballFactory(directory.toFile());
    }

    /*
     * (non-Javadoc)
     * @see org.oddjob.oddballs.OddballFactory#createFrom(java.io.File, java.lang.ClassLoader)
     */
    @Override
    public Oddball createFrom(ClassLoader parentLoader) {

        if (!directory.isDirectory()) {
            return null;
        }

        try {
            File[] files = classpathURLs(directory);
            if (files.length > 0) {
                logger.info("Adding Oddball [{}]", directory.getPath());
                return ClasspathOddball.from(parentLoader, files);
            }
            else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    File[] classpathURLs(File parent) throws IOException {

        File[] jars = new File(parent, "lib").listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        // no lib dir.
        if (jars == null) {
            jars = new File[0];
        }

        int offset;
        File[] files;

        File classesDir = new File(parent, "classes");
        if (classesDir.exists()) {
            files = new File[jars.length + 1];
            files[0] = classesDir.getCanonicalFile();
            offset = 1;
        } else {
            files = new File[jars.length];
            offset = 0;
        }

        for (int i = 0; i < jars.length; ++i) {
            files[i + offset] = jars[i].getCanonicalFile();
        }

        return files;
    }
}
