package org.oddjob.sql;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Something that is able to extract a value from a column based on the
 * SQL Type.
 * 
 * @author rob
 *
 */
public abstract class ColumnExtractor<T> {
		
	public static final ColumnExtractor<Boolean> BOOLEAN_EXTRACTOR = 
			new ColumnExtractor<Boolean>() {
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

	public static final ColumnExtractor<Short> SHORT_EXTRACTOR = 
			new ColumnExtractor<Short>() {
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
	
	public static final ColumnExtractor<Integer> INT_EXTRACTOR = 
			new ColumnExtractor<Integer>() {
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
	
	public static final ColumnExtractor<Long> LONG_EXTRACTOR = 
			new ColumnExtractor<Long>() {
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
	
	public static final ColumnExtractor<Double> DOUBLE_EXTRACTOR = 
			new ColumnExtractor<Double>() {
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
	
	public static final ColumnExtractor<BigDecimal> BIG_DECIMAL_EXTRACTOR = 
			new ColumnExtractor<BigDecimal>() {
		@Override
		public java.lang.Class<BigDecimal> getType() {
			return BigDecimal.class;
		}
		@Override
		public BigDecimal extract(ResultSet rs, int columnIndex) 
		throws SQLException {
			BigDecimal column = rs.getBigDecimal(columnIndex);
			if (rs.wasNull()) {
				return null;
			}
			else {
				return column;
			}
		}
	};
	
	public static final ColumnExtractor<String> STRING_EXTRACTOR = 
			new ColumnExtractor<String>() {
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
	
	public static final ColumnExtractor<Date> DATE_EXTRACTOR = 
			new ColumnExtractor<Date>() {
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
	
	public static final ColumnExtractor<Time> TIME_EXTRACTOR = 
			new ColumnExtractor<Time>() {
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
	
	public static final ColumnExtractor<Timestamp> TIMESTAMP_EXTRACTOR = 
			new ColumnExtractor<Timestamp>() {
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
	
	public static final ColumnExtractor<Object> DEFAULT_EXTRACTOR = 
			new ColumnExtractor<Object>() {
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
	
	private static final Map<Integer, ColumnExtractor<?>> EXTRACTOR_TYPES = 
			new HashMap<Integer, ColumnExtractor<?>>();
	
	static {
		EXTRACTOR_TYPES.put(Types.BIT, 			SHORT_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.TINYINT, 		SHORT_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.SMALLINT, 	SHORT_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.INTEGER, 		INT_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.BIGINT, 		LONG_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.FLOAT, 		DOUBLE_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.REAL, 		DOUBLE_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.DOUBLE, 		DOUBLE_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.NUMERIC, 		BIG_DECIMAL_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.DECIMAL, 		BIG_DECIMAL_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.CHAR, 		STRING_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.VARCHAR, 		STRING_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.LONGVARCHAR,	STRING_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.DATE, 		DATE_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.TIME, 		TIME_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.TIMESTAMP, 	TIMESTAMP_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.BOOLEAN, 		BOOLEAN_EXTRACTOR);
		EXTRACTOR_TYPES.put(Types.ROWID, 		LONG_EXTRACTOR);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> ColumnExtractor<X> getColumnExtractor(Integer sqlType) {
		return (ColumnExtractor<X>) EXTRACTOR_TYPES.get(sqlType);
	}
	
	/**
	 * Get the class of this Column Extractor.
	 * 
	 * @return The class, never null.
	 */
	abstract public Class<T> getType();
	
	/**
	 * Extract the value from the column.
	 * 
	 * @param rs
	 * @param columnIndex
	 * @return
	 * @throws SQLException
	 */
	abstract public T extract(ResultSet rs, int columnIndex) throws SQLException;
	
}
