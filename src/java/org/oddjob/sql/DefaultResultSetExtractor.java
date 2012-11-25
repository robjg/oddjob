package org.oddjob.sql;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

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
public class DefaultResultSetExtractor implements ResultSetExtractor {
	
	public interface Extractor<T> {
		
		public Class<T> getType();
		
		public T extract(ResultSet rs, int columnIndex) throws SQLException;
	}
	
	private static final Map<Integer, Extractor<?>> EXTRACTOR_TYPES = 
			new HashMap<Integer, Extractor<?>>();
	
	public static final Extractor<Boolean> BOOLEAN_EXTRACTOR = 
			new Extractor<Boolean>() {
		@Override
		public java.lang.Class<Boolean> getType() {
			return Boolean.class;
		}
		@Override
		public Boolean extract(ResultSet rs, int columnIndex) 
		throws SQLException {
			Boolean column = new Boolean(rs.getBoolean(columnIndex));
			if (rs.wasNull()) {
				return null;
			}
			else {
				return column;
			}
		}
	};

	public static final Extractor<Short> SHORT_EXTRACTOR = 
			new Extractor<Short>() {
		@Override
		public java.lang.Class<Short> getType() {
			return Short.class;
		}
		@Override
		public Short extract(ResultSet rs, int columnIndex) 
		throws SQLException {
			Short column = new Short(rs.getShort(columnIndex));
			if (rs.wasNull()) {
				return null;
			}
			else {
				return column;
			}
		}
	};
	
	public static final Extractor<Integer> INT_EXTRACTOR = 
			new Extractor<Integer>() {
		@Override
		public java.lang.Class<Integer> getType() {
			return Integer.class;
		}
		@Override
		public Integer extract(ResultSet rs, int columnIndex) 
		throws SQLException {
			Integer column = new Integer(rs.getInt(columnIndex));
			if (rs.wasNull()) {
				return null;
			}
			else {
				return column;
			}
		}
	};
	
	public static final Extractor<Long> LONG_EXTRACTOR = 
			new Extractor<Long>() {
		@Override
		public java.lang.Class<Long> getType() {
			return Long.class;
		}
		@Override
		public Long extract(ResultSet rs, int columnIndex)
		throws SQLException {
			Long column = new Long(rs.getLong(columnIndex));
			if (rs.wasNull()) {
				return null;
			}
			else {
				return column;
			}
		}
	};
	
	public static final Extractor<Double> DOUBLE_EXTRACTOR = 
			new Extractor<Double>() {
		@Override
		public java.lang.Class<Double> getType() {
			return Double.class;
		}
		@Override
		public Double extract(ResultSet rs, int columnIndex) 
		throws SQLException {
			Double column = new Double(rs.getDouble(columnIndex));
			if (rs.wasNull()) {
				return null;
			}
			else {
				return column;
			}
		}
	};
	
	public static final Extractor<String> STRING_EXTRACTOR = 
			new Extractor<String>() {
		@Override
		public java.lang.Class<String> getType() {
			return String.class;
		}
		@Override
		public String extract(ResultSet rs, int columnIndex)
		throws SQLException {
			return rs.getString(columnIndex);
		}
	};
	
	public static final Extractor<Date> DATE_EXTRACTOR = 
			new Extractor<Date>() {
		@Override
		public java.lang.Class<Date> getType() {
			return Date.class;
		}
		@Override
		public Date extract(ResultSet rs, int columnIndex)
		throws SQLException {
			return rs.getDate(columnIndex);
		}
	};
	
	public static final Extractor<Time> TIME_EXTRACTOR = 
			new Extractor<Time>() {
		@Override
		public java.lang.Class<Time> getType() {
			return Time.class;
		}
		@Override
		public Time extract(ResultSet rs, int columnIndex) 
		throws SQLException {
			return rs.getTime(columnIndex);
		}
	};
	
	public static final Extractor<Timestamp> TIMESTAMP_EXTRACTOR = 
			new Extractor<Timestamp>() {
		@Override
		public java.lang.Class<Timestamp> getType() {
			return Timestamp.class;
		}
		@Override
		public Timestamp extract(ResultSet rs, int columnIndex) 
		throws SQLException {
			return rs.getTimestamp(columnIndex);
		}
	};
	
	public static final Extractor<Object> DEFAULT_EXTRACTOR = 
			new Extractor<Object>() {
		@Override
		public java.lang.Class<Object> getType() {
			return Object.class;
		}
		@Override
		public Object extract(ResultSet rs, int columnIndex) 
		throws SQLException {
			return rs.getObject(columnIndex);
		}
	};
	
	static {
		EXTRACTOR_TYPES.put(Types.BIT, 			SHORT_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.TINYINT, 		SHORT_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.SMALLINT, 	SHORT_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.INTEGER, 		INT_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.BIGINT, 		LONG_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.FLOAT, 		DOUBLE_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.REAL, 		DOUBLE_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.DOUBLE, 		DOUBLE_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.NUMERIC, 		DOUBLE_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.DECIMAL, 		DOUBLE_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.CHAR, 		STRING_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.VARCHAR, 		STRING_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.LONGVARCHAR,	STRING_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.DATE, 		DATE_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.TIME, 		TIME_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.TIMESTAMP, 	TIMESTAMP_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.BOOLEAN, 		BOOLEAN_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.ROWID, 		LONG_EXTRACTOR);
	}
	
	private final ResultSet resultSet;
	
	private final Extractor<?>[] columnTypes;
	
	private final String[] columnNames;

	private final int columnCount;
	
	public DefaultResultSetExtractor(ResultSet rs) 
	throws SQLException {
		
		this.resultSet = rs;
				
		ResultSetMetaData metaData = rs.getMetaData();
		
		this.columnCount = metaData.getColumnCount();
				
		this.columnTypes = new Extractor[columnCount];
		this.columnNames = new String[columnCount];
		
		for (int i = 0; i < columnTypes.length; ++i) {
			
			int columnType = metaData.getColumnType(i + 1);
			Extractor<?> extractor = EXTRACTOR_TYPES.get(columnType);
			if (extractor == null) {
				extractor = DEFAULT_EXTRACTOR;
			}
			columnTypes[i] = extractor;
			
			columnNames[i] = metaData.getColumnName(i + 1);
		}
	}
	
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
	
}
