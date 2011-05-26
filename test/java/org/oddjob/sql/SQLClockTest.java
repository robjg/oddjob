package org.oddjob.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.io.BufferType;

public class SQLClockTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(SQLClockTest.class);
	
	ConnectionType ct;
	
	@Override
	protected void setUp() throws Exception {
		ct = new ConnectionType();
		ct.setDriver("org.hsqldb.jdbcDriver");
		ct.setUrl("jdbc:hsqldb:mem:test");
		ct.setUsername("sa");
		ct.setPassword("");
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
	
	public void testGetTime() throws SQLException, ArooaConversionException {
	
		Connection c = ct.toValue();
		
		SQLClock test = new SQLClock();
		test.setConnection(c);
		test.start();
		
		Date date = test.getClock().getDate();
		
		assertNotNull(date);
		
		logger.info(date);
		
		test.stop();
	}
}
