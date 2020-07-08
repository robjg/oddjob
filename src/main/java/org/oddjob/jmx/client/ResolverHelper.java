package org.oddjob.jmx.client;

import org.oddjob.arooa.ClassResolver;

import java.util.Arrays;
import java.util.Objects;

/**
 * TODO remove.
 * 
 * @author rob
 *
 */
public class ResolverHelper {

	private final ClassResolver classResolver;
	
	public ResolverHelper(ClassResolver classResolver) {
		this.classResolver = classResolver;
	}

	public Class<?>[] resolve(String[] classNames) {
		return Arrays.stream(classNames).map(name -> classResolver.findClass(name))
				.filter(Objects::nonNull)
				.toArray(Class[]::new);
	}
}
