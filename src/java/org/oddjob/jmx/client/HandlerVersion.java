package org.oddjob.jmx.client;

import java.io.Serializable;

public class HandlerVersion implements Serializable {
	private static final long serialVersionUID = 2009090500L;
	
	private final int major;
	
	private final int minor;
	
	public HandlerVersion(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}
	
	public int getMajor() {
		return major;
	}
	
	public int getMinor() {
		return minor;
	}
	
	@Override
	public String toString() {
		return "Handler Version: " + major + "." + minor;
	}
}
