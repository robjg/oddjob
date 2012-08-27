package org.oddjob.util;

/**
 * Used by State Handlers when an attempt to acquire a lock timeouts.
 *
 * @author Rob Gordon.
 */
public class OddjobLockTimeoutException extends RuntimeException {
	private static final long serialVersionUID = 2012082700L;

	public OddjobLockTimeoutException(String msg) {
		super(msg);
	}
}
