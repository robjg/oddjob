package org.oddjob.sql;
import org.junit.Before;
import org.junit.After;

import org.junit.Test;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.io.BufferType;
import org.oddjob.persist.SerializeWithBinaryStream;
import org.oddjob.persist.SerializeWithBytes;

public class HSQLAssumptionsTest extends OjTestCase {
	
	private static final Logger logger = Logger.getLogger(HSQLAssumptionsTest.class);
	
	ConnectionType ct;
	
    @Before
    public void setUp() throws Exception {
		ct = new ConnectionType();
		ct.setDriver("org.hsqldb.jdbcDriver");
		ct.setUrl("jdbc:hsqldb:mem:test");
		ct.setUsername("sa");
		ct.setPassword("");
		
		BufferType buffer = new BufferType();
		buffer.setText("CREATE TABLE test(" +
				"key VARCHAR(128), " +
				"job_as_stream BLOB, " +
				"job_as_bytes BLOB, " +
				"CONSTRAINT test_pk PRIMARY KEY (key))");
		buffer.configured();
		
		SQLJob sql = new SQLJob();
		sql.setArooaSession(new StandardArooaSession());
		sql.setInput(buffer.toInputStream());
		
		sql.setConnection(ct.toValue());
		
		sql.run();
	}
	
    @After
    public void tearDown() throws Exception {
		
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

	/**
	 * Tests because HSQL Version 2 didn't look to support blobs as expected.
	 * <p>
	 * - Looks like they've been fixed in version 2.2.5
	 * 
	 * @author rob
	 *
	 */
   @Test
	public void testBytes() throws SQLException, ArooaConversionException {
		
		Connection connection = ct.toValue();
		
		PreparedStatement insert = connection.prepareStatement(
				"insert into test (key, job_as_stream, job_as_bytes)" +
				" values (?, ?, ?)");
		
		Object job = new BigThing();

		byte[] bytes = new SerializeWithBytes().toBytes(job);

		logger.debug("Saving: " + 
				bytes.length + " bytes.");
		
		insert.setString(1, "a");
	
		insert.setBlob(2, new SerializeWithBinaryStream().toStream(job));
		
		insert.setBytes(3, bytes);
		
		insert.executeUpdate();
		
		PreparedStatement select = connection.prepareStatement(
				"select job_as_stream, job_as_bytes from test where key = ?");
		
		select.setString(1, "a");
		
		ResultSet rs = select.executeQuery();
		
		assertTrue(rs.next());
		
		byte[] bytesCopy = rs.getBytes(1);
		
		Object copy1 = new SerializeWithBytes().fromBytes(
				bytesCopy, 
				getClass().getClassLoader());
		
		assertNotNull(copy1);
		
		Blob blob = rs.getBlob(2);
		
		Object copy2 = new SerializeWithBinaryStream().fromStream(
				blob.getBinaryStream(), 
				getClass().getClassLoader());
		
		assertNotNull(copy2);
		
		insert.close();
		select.close();
		
		connection.close();
	}
	
}
