package org.oddjob.state;

/**
 * 
 * @oddjob.description
 * 
 * The namespace version this job is deprecated. Please use 
 * {@link CascadeJob} instead.
 * 
 * @deprecated Use
 * @author rob
 *
 */
public class CascadeJobDepricated extends CascadeJob {
	private static final long serialVersionUID = 2015041700L;

	public CascadeJobDepricated() {
		logger().error("The namespace version of " + 
				super.getClass().getSimpleName() + 
				"is depricated. Please use the none namespace version.");
	}
}
