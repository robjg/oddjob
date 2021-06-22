package org.oddjob.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles results from a query.
 * 
 * @author rob
 *
 */
public interface SQLResultHandler {

	void handleResultSet(ResultSet resultSet,
			DatabaseDialect dialect)
	throws SQLException, ClassNotFoundException;

	void handleUpdate(int updateCount,
			DatabaseDialect dialect)
	throws SQLException, ClassNotFoundException;
}
