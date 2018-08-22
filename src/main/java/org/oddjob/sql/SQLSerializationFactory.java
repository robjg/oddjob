package org.oddjob.sql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A Factory for {@link SQLSerialization}.
 * 
 * @author rob
 *
 */
public interface SQLSerializationFactory {
	
	/**
	 * Create a {@link SQLSerialization}.
	 * 
	 * @param connection
	 * 
	 * @return A SQLSerialization. Never null.
	 * 
	 * @throws SQLException
	 */
	public SQLSerialization createSerialization(Connection connection)
	throws SQLException;
	
}
