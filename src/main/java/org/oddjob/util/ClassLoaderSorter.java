package org.oddjob.util;

/**
 * A utility class that finds the highest (in the ClassLoader hierarchy) 
 * ClassLoader for the given classes.
 * <p>
 * This is required to create a dynamic proxy when the interfaces it 
 * implements are not in the same class loader.
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
		
		ClassLoader topLoader = null;
		for (Class<?> cl : forClasses) {
			for (ClassLoader checkLoader = cl.getClassLoader();
				true; checkLoader = checkLoader.getParent()) {
				if (checkLoader == topLoader) {
					topLoader = cl.getClassLoader();
					break;
				}
				if (checkLoader == null) {
					break;
				}
			}
		}		
		return topLoader;
	}	
}
