package org.oddjob.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.oddjob.beanbus.BusConductor;

/**
 * Handles results from a query.
 * 
 * @author rob
 *
 */
public interface SQLResultHandler {

	public void handleResultSet(ResultSet resultSet, 
			DatabaseDialect dialect)
	throws SQLException, ClassNotFoundException;

	public void handleUpdate(int updateCount,
			DatabaseDialect dialect)
	throws SQLException, ClassNotFoundException;
		
	public void setBusConductor(BusConductor busConductor);
}
