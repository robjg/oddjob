package org.oddjob.describe;

import java.lang.reflect.Method;
import java.util.Map;

import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.reflect.PropertyAccessor;

/**
 * A {@link Describer} that looks for a method annotated with 
 * {@link DescribeWith} that will provide a description.
 * 
 * @author rob
 *
 */
public class AnnotationDescriber implements Describer {

	private final ArooaSession session;
	
	/**
	 * Constructor.
	 * 
	 * @param session The session that provides the descriptor used
	 * to check for the annotation.
	 */
	public AnnotationDescriber(ArooaSession session) {
		if (session == null) {
			throw new NullPointerException("Session is null.");
		}
		this.session = session;
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> describe(Object bean) {
		
		PropertyAccessor accessor = session.getTools().getPropertyAccessor();
	
		ArooaBeanDescriptor descriptor = 
				session.getArooaDescriptor().getBeanDescriptor(
						accessor.getClassName(bean), accessor);
		
		Method method = descriptor.getAnnotations().methodFor(
				DescribeWith.class.getName());
		
		if (method == null) {
			return null;
		}
			
		try {
			return (Map<String, String>) method.invoke(bean);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
