package org.oddjob;

/**
 * Allow something to be forced. This is intended to be used to
 * force the state of a job to COMPLETE, but this intent is not 
 * captured in the name of this interface or the corresponding job
 * action as other uses may be found.
 * 
 * @author rob
 *
 */
public interface Forceable {

	/**
	 * Force something. Generally this will be the state of a job
	 * to COMPLETE but other uses may be found.
	 */
	public void force();
}
