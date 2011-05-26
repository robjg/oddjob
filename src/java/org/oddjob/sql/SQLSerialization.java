package org.oddjob.sql;

import java.sql.SQLException;

import org.oddjob.arooa.registry.Path;

/**
 * Abstraction for something that is able to serialize and
 * deserialization, using SQL.
 * 
 * @author rob
 *
 */
public interface SQLSerialization {

	public void persist(Path path, String id, 
			Object o) throws SQLException;
		
	public Object restore(Path path, String id, 
			ClassLoader classLoader) throws SQLException;
		
	public void remove(Path path, String id) throws SQLException;
		
	public String[] children(Path path) throws SQLException;
	
	public void clear(Path path) throws SQLException;
	
	public void close() throws SQLException;
}
