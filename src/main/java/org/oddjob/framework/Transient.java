/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

/**
 * A marker interface to indicate that an object is
 * not {link java.io.Serializeable}.
 * <p>
 * It is required because Proxy implements Serializable
 * so we need to be able to override this for proxying
 * objects that aren't serializable.
 *
 */
public interface Transient {

}
