package org.oddjob.util;

import org.oddjob.OddjobException;

import java.util.Objects;

/**
 * Intended to wrap checked exceptions in {@code Runnable}s and other places without a throw clause.
 * Better than just using RuntimeException as it makes it
 * easier to spot that this isn't the actual exception.
 * These exceptions will always have a cause.
 */
public class OddjobWrapperException extends OddjobException {
    private static final long serialVersionUID = 2020101500L;

    public OddjobWrapperException(String s, Throwable t) {
        super(s, Objects.requireNonNull(t));
    }

    public OddjobWrapperException(Throwable t) {
        super(Objects.requireNonNull(t));
    }
}
