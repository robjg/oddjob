/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Optional;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;

/**
 * @author Rob Gordon.
 * @oddjob.description Specify a file. In addition to being useful for
 * configuring a job property that requires a file, this type can be used
 * wherever an input or output is required.
 * <p>
 * This type always resolves the canonical file name.
 * @oddjob.example Set the {@link CopyJob} file copy example.
 */
public class FileType implements ArooaValue, Serializable {
    private static final long serialVersionUID = 2012042000L;

    public static class Conversions implements ConversionProvider {

        public void registerWith(ConversionRegistry registry) {

            Convertlet<FileType, File> fileConvertlet = from -> {
                try {
                    return from.toCanonicalFile();
                } catch (IOException e) {
                    throw new ConvertletException(
                            "File ]" + from.file +
                                    "] is not in a Canonical Form.", e);
                }
            };

            registry.register(FileType.class, File.class, fileConvertlet);

            registry.register(FileType.class, File[].class,
                    from -> Optional.ofNullable(fileConvertlet.convert(from))
                            .map(f -> new File[]{f})
                            .orElse(null));

            Convertlet<FileType, Path> pathConvertlet = from -> Optional.ofNullable(fileConvertlet.convert(from))
                    .map(File::toPath)
                    .orElse(null);

            registry.register(FileType.class, Path.class, pathConvertlet);

            registry.register(FileType.class, Path[].class,
                    from -> Optional.ofNullable(pathConvertlet.convert(from))
                            .map(p -> new Path[]{p})
                            .orElse(null));
        }
    }

    /**
     * @oddjob.property
     * @oddjob.description The file path.
     * @oddjob.required Yes.
     */
    private File file;

    /**
     * Getter for file.
     *
     * @return
     */
    public File getFile() {
        return file;
    }

    /**
     * The Canonical form of the file.
     *
     * @return The Canonical form.
     * @throws IOException
     */
    public File toCanonicalFile() throws IOException {
        return file == null ? null : file.getCanonicalFile();
    }

    /**
     * Set the file.
     *
     * @param file The file.
     * @throws IOException
     */
    @ArooaAttribute
    public void setFile(File file) {
        this.file = file;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "File " + getFile();
    }

}
