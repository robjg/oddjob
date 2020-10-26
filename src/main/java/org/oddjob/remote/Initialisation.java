package org.oddjob.remote;

import java.io.Serializable;

/**
 * Wraps Initialisation Data as it is passed to a client.
 *
 * @see Implementation
 *
 * @param <T> The type of the data.
 */
public class Initialisation<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 2020102400L;

    private final Class<T> type;

    private final T data;

    public Initialisation(Class<T> type, T data) {
        this.type = type;
        this.data = data;
    }

    public static <T extends Serializable> Initialisation<T> from(Class<T> type, T data) {
        return new Initialisation<>(type, data);
    }

    public Class<T> getType() {
        return type;
    }

    public T getData() {
        return data;
    }
}
