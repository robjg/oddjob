package org.oddjob.sql;

import java.sql.SQLException;

import org.oddjob.beanbus.BadBeanException;
import org.oddjob.beanbus.Destination;

/**
 * Abstraction for something that executes an SQL statement.
 * 
 * @author rob
 *
 */
public interface SQLExecutor extends Destination<String> {

	/**
	 * Execute the SQL.
	 * 
	 * @param sql The SQL. Never expected to be null.
	 * 
	 * @throws SQLException
	 */
    public void accept(String sql) throws BadBeanException;

}
