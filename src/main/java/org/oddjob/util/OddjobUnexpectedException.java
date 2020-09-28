/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.util;

import org.oddjob.OddjobException;

/**
 * An Unchecked Exception for when something is not as expected. For unexpected checked exceptions use
 * {@link OddjobWrapperException}.
 *
 * @author Rob Gordon.
 */
public class OddjobUnexpectedException extends OddjobException {
	private static final long serialVersionUID = 2020092800L;

	public OddjobUnexpectedException(String msg) {
		super(msg);
	}
}
