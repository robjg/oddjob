package org.oddjob.util;

/**
 * A utility class that finds the highest (in the ClassLoader hierarchy) 
 * ClassLoader for the given classes.
 * <p>
 * TODO: The current implementation assumes that all classes are in the
 * same ClassLoader hierarchy but doesn't validate this. It should.
 * 
 * @author rob
 *
 */
public class ClassLoaderSorter {

	/**
	 * Find the highest required ClassLoader.
	 * 
	 * @param forClasses The Classes.
	 * 
	 * @return The highest ClassLoader, possibly the System ClassLoader, 
	 * but never null.
	 */
	public ClassLoader getTopLoader(Class<?>[] forClasses) {
		
		ClassLoader topLoader = ClassLoader.getSystemClassLoader();
		for (Class<?> cl : forClasses) {
			for (ClassLoader checkLoader = cl.getClassLoader();
				checkLoader != null; checkLoader = checkLoader.getParent()) {
				if (checkLoader == topLoader) {
					topLoader = cl.getClassLoader();
					break;
				}
			}
		}		
		return topLoader;
	}	
}
