package org.oddjob.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provide some flexibility for differing databases.
 * 
 * @author rob
 *
 */
public interface DatabaseDialect {

	/**
	 * Provide an {@link ResultSetExtractor}.
	 * 
	 * @param resultSet The result set.
	 * 
	 * @return The extranctor. never null.
	 * 
	 * @throws SQLException
	 */
	public ResultSetExtractor resultSetExtractorFor(ResultSet resultSet)
	throws SQLException;
}
