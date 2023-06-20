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
@Deprecated
public class CascadeJobDeprecated extends CascadeJob {
	private static final long serialVersionUID = 2015041700L;

	public CascadeJobDeprecated() {
		logger().error("The namespace version of " + 
				super.getClass().getSimpleName() + 
				"is deprecated. Please use the none namespace version.");
	}
}
