package org.oddjob.framework;

/**
 * Allow Components to be optionally transient by specifying the transient
 * property is true;
 */
public interface OptionallyTransient {

	/**
	 * Is the component transient.
	 * 
	 * @return true if transient, false otherwise.
	 */
	boolean isTransient();
}
