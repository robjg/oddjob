package org.oddjob.describe;

import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.ArooaAnnotation;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Describe the properties of an object using a {@link PropertyAccessor}.
 * <p>
 * If a property is annotated with {@link NoDescribe} then it will not
 * be described. 
 * 
 * 
 * @author rob
 *
 */
public class AccessorDescriber implements Describer {

	private final ArooaSession session;
	
	/**
	 * Constructor.
	 * 
	 * @param session The session that proves annotations from the
	 * descriptor and an accessor from the tools.
	 */
	public AccessorDescriber(ArooaSession session) {
		if (session == null) {
			throw new NullPointerException("Session is null.");
		}
		this.session = session;
	}
	
	@Override
	public Map<String, String> describe(Object bean) {

		PropertyAccessor accessor = 
				session.getTools().getPropertyAccessor();
		
		ArooaClass arooaClass = accessor.getClassName(bean);
		
		BeanOverview overview = arooaClass.getBeanOverview(
				accessor);
		
		ArooaAnnotations annotations = 
				session.getArooaDescriptor().getBeanDescriptor(
						arooaClass, accessor).getAnnotations();
		
		Map<String, String> description = new TreeMap<>();

		String[] properties = overview.getProperties();
		
		for (String property : properties ) {
			
			if (overview.hasReadableProperty(property) &&
					!overview.isIndexed(property) &&
					!overview.isMapped(property)) {

				ArooaAnnotation annotation = 
						annotations.annotationForProperty(
								property, NoDescribe.class.getName());
				if (annotation != null) {
					continue;
				}
				
				Object value = accessor.getProperty(bean, property);
				
				if (value == null) {
					description.put(property, null);
				}
				else if (value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive()) {
						description.put(property,
								Arrays.toString((Object[]) value));
				}
				else {
					description.put(property, value.toString());
				}
			}
		}
		
		return description;
	}	
}
