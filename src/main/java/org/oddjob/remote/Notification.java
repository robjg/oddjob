package org.oddjob.remote;

import java.io.Serializable;

/**
 * A Notification.
 */
public class Notification implements Serializable {

    private static final long serialVersionUID = 2020062900L;

    private final String type;

    private final long sequenceNumber;

    private final Object userData;

    public Notification(String type, long sequenceNumber, Object userData) {
        this.sequenceNumber = sequenceNumber;
        this.type = type;
        this.userData = userData;
    }

    public long getSequenceNumber() {
        return sequenceNumber ;
    }

    public String getType() {
        return type;
    }

    public Object getUserData() {
        return userData;
    }


}
