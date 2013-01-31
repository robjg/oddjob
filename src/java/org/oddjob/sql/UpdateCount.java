package org.oddjob.sql;

/**
 * Container bean for the update count from an {@link SQLJob}.
 * 
 * @author rob
 *
 */
public class UpdateCount {

	private final int count;
	
	public UpdateCount(int count) {
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": count=" + count;
	}
}
