package org.oddjob.events.example;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class AtomicCopy implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AtomicCopy.class);

    private volatile String name;

    private volatile Path from;

    private volatile Path to;

    @Override
    public void run() {

        Path from = Optional.ofNullable(this.from)
                .orElseThrow(() -> new IllegalArgumentException("No from."));

        Path to = Optional.ofNullable(this.to)
                .orElseThrow(() -> new IllegalArgumentException("No from."));

        if (!Files.isDirectory(to)) {
            throw new IllegalArgumentException("To not a directory");
        }

        String tmpName = from.getFileName() + "_tmp";
        Path temp = to.resolve(tmpName);

        logger.info("Copying {} to {}.", from, to);

        try {
            Files.copy(from, temp);
            Files.move(temp, to.resolve(from.getFileName()), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getFrom() {
        return from;
    }

    public void setFrom(Path from) {
        this.from = from;
    }

    public Path getTo() {
        return to;
    }

    @ArooaAttribute
    public void setTo(Path to) {
        this.to = to;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}
