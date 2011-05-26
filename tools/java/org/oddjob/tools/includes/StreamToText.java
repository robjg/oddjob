package org.oddjob.tools.includes;

import java.io.IOException;
import java.io.InputStream;

public interface StreamToText {

	public String load(InputStream input)
	throws IOException;
}
