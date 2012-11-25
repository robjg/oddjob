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

	public ResultSetExtractor resultSetExtractorFor(ResultSet resultSet)
	throws SQLException;
}
