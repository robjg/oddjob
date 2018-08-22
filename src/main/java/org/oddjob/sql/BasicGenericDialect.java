package org.oddjob.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The simplest most generic dialect. The default that {@link SQLJob}
 * uses.
 * 
 * @author rob
 *
 */
public class BasicGenericDialect implements DatabaseDialect {

	private static final Logger logger = LoggerFactory.getLogger(BasicGenericDialect.class);
	
	private ClassLoader classLoader;	
	
	@Override
	public ResultSetExtractor resultSetExtractorFor(
			final ResultSet resultSet)
	throws SQLException {
		
		ResultSetMetaData metaData = resultSet.getMetaData();
		
		final int columnCount = metaData.getColumnCount();
		
		final Class<?>[] columnTypes = new Class<?>[columnCount];
		final String[] columnNames = new String[columnCount];
		
		ClassLoader classLoader = this.classLoader;
		if (classLoader == null) {
			classLoader = metaData.getClass().getClassLoader();
		}
		
		for (int i = 0; i < columnCount ; ++i) {
			
			columnNames[i] = metaData.getColumnName(i + 1);
			String typeName = metaData.getColumnClassName(i + 1);
			try {
				columnTypes[i] = Class.forName(typeName, true, classLoader);
			} catch (ClassNotFoundException e) {
				logger.warn("Can't Load Class " + typeName + ", defaulting to Object.");
			}
		}

		if (logger.isDebugEnabled()) {
			StringBuilder message = new StringBuilder("Columns and Types:");
			for (int i = 0; i < columnCount; ++i) {
				if (i > 0) {
					message.append(", ");
				}
				message.append(columnNames[i]);
				message.append("=");
				message.append(columnTypes[i].getName());
			}
			logger.debug(message.toString());
		}
		
		return new ResultSetExtractor() {
			
			@Override
			public Class<?> getColumnType(int columnIndex) {
				return columnTypes[columnIndex - 1];
			}
			
			@Override
			public Object getColumn(int columnIndex) throws SQLException {
				return resultSet.getObject(columnIndex);
			}
			
			@Override
			public int getColumnCount() {
				return columnCount;
			}
			
			@Override
			public String getColumnName(int columnIndex) {
				return columnNames[columnIndex - 1];
			}
			
			@Override
			public boolean next() throws SQLException {
				return resultSet.next();
			}
		};
	}
	


	public ClassLoader getClassLoader() {
		return classLoader;
	}


	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

}
