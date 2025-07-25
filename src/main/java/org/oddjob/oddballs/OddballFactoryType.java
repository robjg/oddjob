package org.oddjob.oddballs;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * @oddjob.description Create an Oddjob from various sources.
 *
 * @oddjob.example
 *
 * Loading two Oddballs.
 * {@oddjob.xml.resource org/oddjob/oddballs/OddballsExample.xml}
 *
 */

public class OddballFactoryType implements ValueFactory<OddballFactory> {

    private static final Logger logger = LoggerFactory.getLogger(OddballFactoryType.class);

    private Path directory;

    private Path[] paths;

    private URL[] urls;

    @Override
    public OddballFactory toValue() throws ArooaConversionException {

        if (directory != null) {

            return DirectoryOddballFactory.from(directory);
        }
        else if (paths != null) {

            return parentLoader -> ClasspathOddball.from(parentLoader, paths);
        }
        else if (urls != null) {

            return parentLoader -> ClasspathOddball.from(parentLoader, urls);
        }

        logger.info("No Oddballs defined.");
        return null;
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    public void setPaths(Path[] paths) {
        this.paths = paths;
    }

    public void setUrls(URL[] urls) {
        this.urls = urls;
    }

    @Override
    public String toString() {

        if (directory != null) {

            return "OddballFactory for directory " + directory;
        }
        else if (paths != null) {

            return "OddballFactory for paths " + Arrays.toString(paths);
        }

        else if (urls != null) {

            return "OddballFactory for urls " + Arrays.stream(urls);
        }
        else {

            return "OddballFactory for nothing yet.";
        }
    }
}
