package org.oddjob.sql;

import org.oddjob.beanbus.BadBeanException;
import org.oddjob.beanbus.BusCrashException;
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
	 * @throws BadBeanException
	 * @throws BusCrashException
	 */
    public void accept(String sql) throws BadBeanException, BusCrashException;

}
