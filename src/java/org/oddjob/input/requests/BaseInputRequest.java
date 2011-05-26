package org.oddjob.input.requests;

import org.oddjob.input.InputRequest;

/**
 * Common to most {@link InputRequest}s.
 * 
 * @author rob
 *
 */
abstract public class BaseInputRequest implements InputRequest {

	/**
	 * @oddjob.property
	 * @oddjob.description The property to set.
	 * @oddjob.required. No. But pointless if missing.
	 */
	private String property;

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}
}
