package org.oddjob.remote;


/**
 * Provides remote operations and notifications.
 */
public interface RemoteConnection extends RemoteInvoker, RemoteNotifier, AutoCloseable {

    /**
     * Allows an implementations to clear up resources after a node has been destroyed.
     *
     * @param remoteId The remote id of the node destroyed.
     */
    void destroy(long remoteId) throws RemoteException;

    @Override
    void close() throws RemoteException;
}
