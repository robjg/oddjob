package org.oddjob.monitor.context;

/**
 * Helper class to search up an {@link ExplorerContext}.
 * 
 * @author rob
 *
 */
public class AncestorSearch {

	/** Context to start search from. */
	private final ExplorerContext start;
	
	/**
	 * Constructor.
	 * 
	 * @param start The context to start the search from.
	 */
	public AncestorSearch(ExplorerContext start) {
		this.start = start;
	}
	
	/**
	 * Get the value of a key. If the start context doesn't
	 * contain the value, then the parent value is examined.  
	 * If that contains null the search continues upwards 
	 * until a value is found or the root is reached.
	 * 
	 * @param key The key to the value.
	 * 
	 * @return The first value in the hierarchy, or null if
	 * none exists.
	 */
	public Object getValue(String key) {
		if (start == null) {
			return null;
		}
		Object value = start.getValue(key);
		if (value == null) {
			return new AncestorSearch(start.getParent()).getValue(key);
		}
		return value;
	}
	
}
