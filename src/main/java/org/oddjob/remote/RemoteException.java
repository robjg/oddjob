package org.oddjob.remote;

/**
 * An exception thrown from remote methods.
 */
public class RemoteException extends Exception {
    static final long serialVersionUID = 2023091300L;

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
