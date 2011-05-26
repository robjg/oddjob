package org.oddjob.sql;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.io.BufferType;
import org.oddjob.persist.SerializeWithBinaryStream;
import org.oddjob.persist.SerializeWithBytes;

public class HSQLAssumptionsTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(HSQLAssumptionsTest.class);
	
	ConnectionType ct;
	
	@Override
	protected void setUp() throws Exception {
		ct = new ConnectionType();
		ct.setDriver("org.hsqldb.jdbcDriver");
		ct.setUrl("jdbc:hsqldb:mem:test");
		ct.setUsername("sa");
		ct.setPassword("");
		
		BufferType buffer = new BufferType();
		buffer.setText("CREATE TABLE test(" +
				"key VARCHAR(128), " +
				"job BLOB, " +
				"CONSTRAINT test_pk PRIMARY KEY (key))");
		buffer.configured();
		
		SQLJob sql = new SQLJob();
		sql.setArooaSession(new StandardArooaSession());
		sql.setInput(buffer.toInputStream());
		
		sql.setConnection(ct.toValue());
		
		sql.run();
	}
	
	@Override
	protected void tearDown() throws Exception {
		
		BufferType buffer = new BufferType();
		buffer.setText("shutdown");
		buffer.configured();
		
		SQLJob sql = new SQLJob();
		sql.setArooaSession(new StandardArooaSession());
		sql.setInput(buffer.toInputStream());
		
		sql.setConnection(ct.toValue());
		
		sql.run();
	}
	
	public static class BigThing implements Serializable {
		private static final long serialVersionUID = 2010043000L;
		
		byte[] bigArray = new byte[1000000];
	}

	public void testBytes() throws SQLException, ArooaConversionException {
		
		Connection connection = ct.toValue();
		
		PreparedStatement insert = connection.prepareStatement(
				"insert into test (key, job) values (?, ?)");
		
		Object job = new BigThing();

		byte[] bytes = new SerializeWithBytes().toBytes(job);

		logger.debug("Saving: " + 
				bytes.length + " bytes.");
		
		insert.setString(1, "a");
	
		try {
			insert.setBlob(2, new SerializeWithBinaryStream().toStream(job));
			fail("HSQL must now support setting blobs as stream.");
		} catch (SQLFeatureNotSupportedException e) {
			// currently expected.
		}
		
		insert.setBytes(2, bytes);
		
		insert.executeUpdate();
		
		PreparedStatement select = connection.prepareStatement(
				"select job from test where key = ?");
		
		select.setString(1, "a");
		
		ResultSet rs = select.executeQuery();
		
		assertTrue(rs.next());
		
		try {
			rs.getBytes(1);
			fail("HSQL must now support getting blobs as bytes.");
		}
		catch (SQLException e) {
			// currently expected;
		}
		
		Blob blob = rs.getBlob(1);
		
		Object copy = new SerializeWithBinaryStream().fromStream(
				blob.getBinaryStream(), 
				getClass().getClassLoader());
		
		assertNotNull(copy);
		
		insert.close();
		select.close();
		
		connection.close();
	}
	
}
