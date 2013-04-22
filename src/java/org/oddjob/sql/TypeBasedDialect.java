package org.oddjob.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * A {@link ResultSetExtractor} that uses simple java types for most
 * column types. This was introduced for Oracle which uses it's own types
 * and this might not always be desirable.
 * <p>
 * This class is still a work in progress.
 * <p>
 * 
 * @author rob
 *
 */
public class TypeBasedDialect implements DatabaseDialect {

	private static final Logger logger = Logger.getLogger(TypeBasedDialect.class);
	
	@Override
	public ResultSetExtractor resultSetExtractorFor(final ResultSet resultSet)
			throws SQLException {
		
		ResultSetMetaData metaData = resultSet.getMetaData();
			
		final int columnCount = metaData.getColumnCount();
			
		final ColumnExtractor<?>[] columnTypes = new ColumnExtractor[columnCount];
		final String[] columnNames = new String[columnCount];
			
		for (int i = 0; i < columnTypes.length; ++i) {

			int columnType = metaData.getColumnType(i + 1);
			ColumnExtractor<?> extractor = 
					ColumnExtractor.getColumnExtractor(columnType);
			
			if (extractor == null) {
				extractor = ColumnExtractor.DEFAULT_EXTRACTOR;
			}
			
			columnTypes[i] = extractor;

			columnNames[i] = metaData.getColumnName(i + 1);
		}
		
		if (logger.isDebugEnabled()) {
			StringBuilder message = new StringBuilder("Columns and Types:\n");
			for (int i = 0; i < columnCount; ++i) {
				message.append(columnNames[i]);
				message.append("=");
				message.append(columnTypes[i].getType().getName());
				message.append("(");
				message.append(ColumnExtractor.SQL_TYPE_NAMES.get(
						metaData.getColumnType(i + 1)));
				message.append(",");
				message.append(metaData.getColumnTypeName(i + 1));
				message.append(")");
				message.append("\n");
			}
			logger.debug(message.toString());
		}
		
		return new ResultSetExtractor() {
			@Override
			public Object getColumn(int columnIndex) throws SQLException {
				return columnTypes[columnIndex - 1].extract(resultSet, columnIndex);
			}
			
			@Override
			public Class<?> getColumnType(int columnIndex) {
				return columnTypes[columnIndex - 1].getType();
			}
			
			@Override
			public String getColumnName(int columnIndex) {
				return columnNames[columnIndex - 1];
			}
			
			@Override
			public int getColumnCount() {
				return columnCount;
			}
			
			@Override
			public boolean next() throws SQLException {
				return resultSet.next();
			}
		};
	}
}
