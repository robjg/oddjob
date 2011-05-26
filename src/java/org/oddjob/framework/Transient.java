/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

/**
 * A marker interface to indicate that an object is
 * not serializeable.
 * <p>
 * It is required becuase Proxy implements serializable
 * so we need to be able to override this for proxying
 * objects that aren't serializable.
 *
 */
public interface Transient {

}
