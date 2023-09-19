package org.oddjob.remote;

/**
 * An {@link RemoteException} generated from a remote component whose id is known.
 */
public class RemoteComponentException extends RemoteException {
    static final long serialVersionUID = 2023091300L;

    private final long remoteId;

    public RemoteComponentException(long remoteId, String message) {
        this(remoteId, message, null);
    }

    public RemoteComponentException(long remoteId, Throwable cause) {
        this(remoteId, null, cause);
    }

    public RemoteComponentException(long remoteId, String message, Throwable cause) {
        super(message, cause);
        this.remoteId = remoteId;
    }

    public long getRemoteId() {
        return remoteId;
    }
}
