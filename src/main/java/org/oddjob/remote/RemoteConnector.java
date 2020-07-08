package org.oddjob.remote;

/**
 * Provides an {@link RemoteConnection}.
 */
public interface RemoteConnector extends AutoCloseable {

    RemoteConnection getConnection() throws RemoteException;

    @Override
    void close() throws RemoteException;
}
