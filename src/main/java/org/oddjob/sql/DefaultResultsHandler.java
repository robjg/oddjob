package org.oddjob.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A Result Handler with Debugs. Simpler to have this than allow a Result Handler to be null.
 */
public class DefaultResultsHandler implements SQLResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultResultsHandler.class);

    @Override
    public void handleResultSet(ResultSet resultSet, DatabaseDialect dialect) throws SQLException, ClassNotFoundException {
        logger.info("Received {} result set of {} columns.",
                resultSet.next() ? "non empty" : "empty",
                resultSet.getMetaData().getColumnCount());
    }

    @Override
    public void handleUpdate(int updateCount, DatabaseDialect dialect) {
        // Ignore this as Executor logs.
    }
}
