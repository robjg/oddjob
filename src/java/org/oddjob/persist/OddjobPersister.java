package org.oddjob.persist;

import org.oddjob.arooa.life.ComponentPersister;

/**
 * Provide a {@link ComponentPersister} for the underlying framework to use.
 * Most implementations will be hierarchical, taking advantage of the id to
 * create a path by which to persist components.
 * <p>
 * Oddjob achieves the hierarchical effect by inspecting 
 * any ComponentPersister passed down from a parent Oddjob. If it is of
 * this type the {@link #persisterFor(String)} method will be
 * called to get the new ComponentPersister. For this reason an 
 * OddjobPersister that wants to be hierarchical must provide a
 * ComponentPersister that is also an OddjobPersister.
 * 
 * @see PersisterBase
 * 
 * @author rob
 *
 */
public interface OddjobPersister {

	/**
	 * Provide a ComponentPersiter which may or may not require the id. If
	 * an id is required but not provided null will be returned.
	 * 
	 * @param id An Id which may be used for a path. May be Null.
	 * @return A ComponentPersister. May be null.
	 */
	public ComponentPersister persisterFor(String id);
	
}
