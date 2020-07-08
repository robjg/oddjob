package org.oddjob.remote;

/**
 * Temporary interface for Legacy JMX Operation Types until they can be removed from Handlers.
 *
 * @param <T>
 */
public interface HasOperationType<T> {

    OperationType<T> getOperationType();
}
