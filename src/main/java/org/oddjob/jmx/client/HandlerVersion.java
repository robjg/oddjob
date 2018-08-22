package org.oddjob.jmx.client;

import java.io.Serializable;

/**
 * Encapsulate a handler version. Client should use the version provided
 * by the server to decide if/how to proxy server side interfaces.
 * 
 * @see SimpleHandlerResolver
 * 
 * @author rob
 *
 */
public class HandlerVersion implements Serializable {
	private static final long serialVersionUID = 2009090500L;
	
	private final int major;
	
	private final int minor;
	
	/**
	 * Constructor.
	 * 
	 * @param major The major version number.
	 * @param minor The minor version number.
	 */
	public HandlerVersion(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}
	
	/**
	 * Get the major version number.
	 * 
	 * @return
	 */
	public int getMajor() {
		return major;
	}
	
	/**
	 * Get the minor version number.
	 * 
	 * @return
	 */
	public int getMinor() {
		return minor;
	}
	
	/**
	 * Return the version as major.minor.
	 * 
	 * @return The version as text.
	 */
	public String getVersionAsText() {
		return + major + "." + minor;
	}
	
	@Override
	public String toString() {
		return "Handler Version: " + getVersionAsText();
	}
}
