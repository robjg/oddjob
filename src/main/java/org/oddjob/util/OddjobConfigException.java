/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.util;

import org.oddjob.OddjobException;
import org.oddjob.arooa.parsing.Location;

/**
 *
 * @author Rob Gordon.
 */
public class OddjobConfigException extends OddjobException {
	private static final long serialVersionUID = 2010071900L;

    public OddjobConfigException(String msg) {
        super (msg);
    }
    
    public OddjobConfigException(String msg, Location location) {
        super (msg + " at " + location.toString());
    }
}
