package org.oddjob.remote;

/**
 * Something that can invoke an operation remotely.
 */
public interface RemoteInvoker {

    <T> T invoke(long remoteId, OperationType<T> operationType, Object... args)
            throws RemoteException;
}
