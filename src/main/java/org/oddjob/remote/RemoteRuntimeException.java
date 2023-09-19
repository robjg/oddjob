package org.oddjob.remote;

/**
 * A wrapper for Remote Exception that allows it to propagate out of interfaces that delegate
 * to remote operations.
 */
public class RemoteRuntimeException extends RuntimeException {
    static final long serialVersionUID = 2023091300L;

    public RemoteRuntimeException(String message, RemoteException cause) {
        super(message, cause);
    }

    public RemoteRuntimeException(RemoteException cause) {
        super(cause);
    }

    @Override
    public synchronized RemoteException getCause() {
        return (RemoteException) super.getCause();
    }
}
