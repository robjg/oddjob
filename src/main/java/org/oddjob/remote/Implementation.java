package org.oddjob.remote;

import java.io.Serializable;
import java.util.Objects;

/**
 * Holds information about an Implementation supported by the Server. Passed to
 * the client as part of {@link org.oddjob.jmx.server.ServerInfo}.
 *
 * @param <T> The type of the Initialisation, if any.
 */
public class Implementation<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 2020102400L;

    private final String type;

    private final String version;

    private final Initialisation<T> initialisation;

    public Implementation(String type, String version, Initialisation<T> init) {

        this.type = Objects.requireNonNull(type);;
        this.version = Objects.requireNonNull(version);

        this.initialisation = init;
    }

    public static <T extends Serializable> Implementation<T> create(String type, String version, Initialisation<T> init) {
        return new Implementation<>(type, version, init);
    }

    public static  Implementation<?> create(String type, String version) {
        return new Implementation<>(type, version, null);
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public Initialisation<T> getInitialisation() {
        return initialisation;
    }
}
