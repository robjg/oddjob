package org.oddjob.describe;

import java.util.Map;

import org.oddjob.arooa.ArooaSession;

/**
 * A composite {@link Describer}.
 * 
 * @see DescribeableDescriber
 * @see AnnotationDescriber
 * @see AccessorDescriber
 * 
 * @author rob
 *
 */
public class UniversalDescriber implements Describer {

	private final Describer[] describers;

	/**
	 * Create the describer.
	 * 
	 * @param session The session used for delegate describers.
	 */
	public UniversalDescriber(ArooaSession session) {
		describers = new Describer[] {
			new DescribeableDescriber(), 
			new AnnotationDescriber(session),
			new AccessorDescriber(session)
		};
	}

	@Override
	public Map<String, String> describe(Object bean) {
		for (int i = 0; i < describers.length; ++i) {
			Map<String, String> description = 
					describers[i].describe(bean);
			if (description != null) {
				return description;
			}
		}
		return null;
	}
}
