package org.oddjob.persist;

import org.oddjob.arooa.life.ComponentPersistException;

/**
 * Something that can be persisted. Used to pass a components save
 * method to helper classes.
 * 
 * @author rob
 *
 */
public interface Persistable {

	public void persist() throws ComponentPersistException;
}
