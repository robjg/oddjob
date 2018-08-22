package org.oddjob.sql;

import java.sql.SQLException;

/**
 * Implementations provide varied methodologies to extract data from a 
 * result set. 
 * <p>
 * All indexes are one based, as with result set.
 * <p>
 * This abstraction mixes meta data and data and so is probably not the
 * best design but it provides enough flexibility for the purposes of 
 * {@link SQLJob}.
 * 
 * @author rob
 *
 */
public interface ResultSetExtractor {

	public Class<?> getColumnType(int columnIndex);
	
	public Object getColumn(int columnIndex) throws SQLException;
	
	public String getColumnName(int columnIndex);
	
	public int getColumnCount();
	
	public boolean next() throws SQLException;
	
}
