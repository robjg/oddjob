package org.oddjob.remote;

/**
 * An {@link RemoteException} for when the remote Id is missing.
 */
public class RemoteUnknownException extends RemoteComponentException {
    static final long serialVersionUID = 2023091300L;

    public RemoteUnknownException(long remoteId, String message) {
        this(remoteId, message, null);
    }

    public RemoteUnknownException(long remoteId, Throwable cause) {
        this(remoteId, null, cause);
    }

    public RemoteUnknownException(long remoteId, String message, Throwable cause) {
        super(remoteId, message, cause);
    }
}
