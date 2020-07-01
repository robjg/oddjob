package org.oddjob.remote;

/**
 * An {@link RemoteException} for when the remote Id is known.
 */
public class RemoteIdException extends RemoteException {

    private final long remoteId;

    public RemoteIdException(long remoteId, String message) {
        this(remoteId, message, null);
    }

    public RemoteIdException(long remoteId, Throwable cause) {
        this(remoteId, null, cause);
    }

    public RemoteIdException(long remoteId, String message, Throwable cause) {
        super(message, cause);
        this.remoteId = remoteId;
    }

}
