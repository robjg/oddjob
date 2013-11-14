package org.oddjob.tools.includes;

/**
 * Loads a file or resource and converts it into HTML to be included in
 * documentation or javadoc.
 * 
 * @author rob
 *
 */
public interface IncludeLoader {

	/**
	 * Can this loader load the give tag.
	 * 
	 * @param tag The tag including the '@'.
	 * @return true if the loader can load the given tag.
	 */
	public boolean canLoad(String tag);
	
	/**
	 * Load the resource of file.
	 * 
	 * @param path The resource or file path.
	 * @return HTML text.
	 */
	public String load(String path);
}
