package org.oddjob.remote;

/**
 * An exception thrown from remote methods.
 */
public class RemoteException extends Exception {

    public RemoteException(String message) {
        super(message);
    }

    public RemoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteException(Throwable cause) {
        super(cause);
    }

    public Throwable getTargetException() {
        return getCause();
    }
}
