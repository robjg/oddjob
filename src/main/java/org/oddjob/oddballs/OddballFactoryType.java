package org.oddjob.oddballs;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * @oddjob.description Create an Oddball from various sources. Primarily intended to be used
 * with the <h href="https://github.com/robjg/oj-resolve>oj-resolve</h> project to load a
 * maven dependency as an Oddball. See {@link OddballsDescriptorFactory}.
 *
 * @oddjob.example
 *
 * Loading two Oddballs.
 * {@oddjob.xml.resource org/oddjob/oddballs/OddballsExample.xml}
 *
 */
public class OddballFactoryType implements ValueFactory<OddballFactory> {

    private static final Logger logger = LoggerFactory.getLogger(OddballFactoryType.class);

    /**
     * @oddjob.property
     * @oddjob.description Paths to create an Oddball from.
     * @oddjob.required Either this or URLs is required.
     */
    private Path[] paths;

    /**
     * @oddjob.property
     * @oddjob.description URLs to create an Oddball from.
     * @oddjob.required Either this or Paths is required.
     */
    private URL[] urls;

    @Override
    public OddballFactory toValue() throws ArooaConversionException {

        if (paths != null) {

            return parentLoader -> ClasspathOddball.from(parentLoader, paths);
        }
        else if (urls != null) {

            return parentLoader -> ClasspathOddball.from(parentLoader, urls);
        }

        logger.info("No Oddballs defined.");
        return null;
    }

    public void setPaths(Path[] paths) {
        this.paths = paths;
    }

    public void setUrls(URL[] urls) {
        this.urls = urls;
    }

    @Override
    public String toString() {

        if (paths != null) {

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
