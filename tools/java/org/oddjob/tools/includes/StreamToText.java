package org.oddjob.tools.includes;

import java.io.IOException;
import java.io.InputStream;

/**
 * Read an {@link InputStream} into a {@link String}.
 * 
 * @author rob
 *
 */
public interface StreamToText {

	public String load(InputStream input)
	throws IOException;
}
