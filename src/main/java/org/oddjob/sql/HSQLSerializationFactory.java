package org.oddjob.sql;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.arooa.registry.Path;
import org.oddjob.persist.SerializeWithBinaryStream;
import org.oddjob.persist.SerializeWithBytes;

/**
 * An implementation {@link SQLSerializationFactory} for HSQLDB but
 * implemented in ANSI SQL so should work with most DBs.
 * <p>
 * This class is synchronised on the statements to avoid what appeared to be
 * deadlock in HSQL but no detailed investigation of this was
 * undertaken.
 * 
 * @author rob
 *
 */
public class HSQLSerializationFactory implements SQLSerializationFactory {

	private String table;

	@Override
	public SQLSerialization createSerialization(Connection connection) 
	throws SQLException {
		return new HSQLSerialization(connection, table);
	}
	
	public void setTable(String tableName) {
		this.table = tableName;
	}
	
	public String getTable() {
		return table;
	}
}

class HSQLSerialization implements SQLSerialization {
	private static final Logger logger = LoggerFactory.getLogger(HSQLSerialization.class);
	
	private final Connection connection;
	
	private final PreparedStatement updateStmt;
	private final PreparedStatement insertStmt;
	private final PreparedStatement selectStmt;
	private final PreparedStatement deleteStmt;
	private final PreparedStatement clearStmt;
	private final PreparedStatement listStmt;

	HSQLSerialization(Connection connection, String tableName) 
	throws SQLException {
		this.connection = connection;
		String table = tableName;
		if (table == null) {
			table = "ODDJOB";
		}
		
		try {
			String insertSQL = "insert into " + table + 
					" (path, id, job) values (?, ?, ?)";
			logger.debug("Preparing: " + insertSQL);
			this.insertStmt = connection.prepareStatement(
					insertSQL);

			String updateSQL = "update " + table + 
					" set job = ? where path = ? and id = ?";
			logger.debug("Preparing: " + updateSQL);
			this.updateStmt = connection.prepareStatement(
					updateSQL);

			String selectSQL = "select job from " + table + 
					" where path = ? and id = ?";
			logger.debug("Preparing: " + selectSQL);
			this.selectStmt = connection.prepareStatement(
					selectSQL);
	
			String deleteSQL = "delete from " + table + 
					" where path = ? and id = ?";
			logger.debug("Preparing: " + deleteSQL);
			this.deleteStmt = connection.prepareStatement(
					deleteSQL);
	
			String clearSQL = "delete from " + table + " where path = ?";
			logger.debug("Preparing: " + clearSQL);
			this.clearStmt = connection.prepareStatement(
					clearSQL);

			String listSQL = "select id from " + table + " where path = ?";
			logger.debug("Preparing: " + listSQL);
			this.listStmt = connection.prepareStatement(
					listSQL);

		} catch (SQLException e) {
			try {
				close();
			}
			catch (SQLException e2) {
				// ignore
			}
			throw e;
		}
	}
	
	@Override
	public synchronized void close() throws SQLException {
		
		SQLException ex = null;
		
		if (updateStmt != null) {
			try {
				updateStmt.close();
			} catch (SQLException e) {
				ex = e;
			}
		}
		
		if (insertStmt != null) {
			try {
				insertStmt.close();
			} catch (SQLException e) {
				ex = e;
			}
		}
		
		if (selectStmt != null) {
			try {
				selectStmt.close();
			} catch (SQLException e) {
				ex = e;
			}
		}
		
		if (deleteStmt != null) {
			try {
				deleteStmt.close();
			} catch (SQLException e) {
				ex = e;
			}
		}
		
		if (clearStmt != null) {
			try {
				clearStmt.close();
			} catch (SQLException e) {
				ex = e;
			}
		}
		
		if (listStmt != null) {
			try {
				listStmt.close();
			} catch (SQLException e) {
				ex = e;
			}
		}
		
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				ex = e;
			}
		}
		
		if (ex != null) {
			throw ex;
		}
	}
		
	@Override
	public void persist(Path path, String id, Object o) throws SQLException {
		
		synchronized (updateStmt) {
			byte[] bytes = new SerializeWithBytes().toBytes(o);
	
			logger.debug("Saving: " +  path + ", " + id + ", " + 
					bytes.length + " bytes.");
			
			updateStmt.setBytes(1, bytes);
			updateStmt.setString(2, path.toString());
			updateStmt.setString(3, id);
	
			int count = updateStmt.executeUpdate();
			if (count == 1) {
				return;
			}
	
			insertStmt.setString(1, path.toString());
			insertStmt.setString(2, id);
			insertStmt.setBytes(3, bytes);
	
			insertStmt.execute();
		}
	}

	@Override
	public Object restore(Path path, String id, 
			ClassLoader classLoader) throws SQLException {

		synchronized (selectStmt) {
			selectStmt.setString(1, path.toString());
			selectStmt.setString(2, id);

			ResultSet rs = selectStmt.executeQuery();
			try {
				if (!rs.next()) {
					return null;
				}

				logger.debug("Retrieved: " +  path + ", " + id + ".");

				Blob blob = rs.getBlob(1);
				InputStream is = blob.getBinaryStream();	
				return new SerializeWithBinaryStream().fromStream(
						is, classLoader);
			}
			finally {
				rs.close();
			}
		}
	}

	@Override
	public void remove(Path path, String id) throws SQLException {
		synchronized (deleteStmt) {
			deleteStmt.setString(1, path.toString());
			deleteStmt.setString(2, id);
			
			deleteStmt.executeUpdate();
		}
	}

	@Override
	public void clear(Path path) throws SQLException {
		synchronized (clearStmt) {
			clearStmt.setString(1, path.toString());
			
			clearStmt.executeUpdate();
		}
	}	
	
	@Override
	public String[] children(Path path) throws SQLException {
		synchronized (listStmt) {
			listStmt.setString(1, path.toString());
			
			ResultSet rs = listStmt.executeQuery();
			try {
				List<String> results = new ArrayList<String>();
				
				while (rs.next()) {
					results.add(rs.getString(1));
				}
				
				return results.toArray(new String[results.size()]);
			}
			finally {
				rs.close();
			}
		}
	}	
}
