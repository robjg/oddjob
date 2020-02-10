/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.util;

/**
 *
 * @author Rob Gordon.
 */
public class OddjobLockedException extends Exception {
	private static final long serialVersionUID = 2010071900L;

	public OddjobLockedException(String msg) {
		super(msg);
	}
}
