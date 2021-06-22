package org.oddjob.sql;

import java.util.Objects;

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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UpdateCount that = (UpdateCount) o;
		return count == that.count;
	}

	@Override
	public int hashCode() {
		return Objects.hash(count);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": count=" + count;
	}
}
