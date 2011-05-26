package org.oddjob.sql;

import org.oddjob.beanbus.Destination;

/**
 * A marker interface for processors of beans from a {@link SQLExecutor}.
 * Implementations must be able to cope with {@link UpdateCount} beans or
 * beans created from a result set.
 * 
 * @author rob
 *
 */
public interface SQLResultsProcessor extends Destination<Object> {

}
