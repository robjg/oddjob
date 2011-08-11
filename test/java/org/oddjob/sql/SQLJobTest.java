/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.sql;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;
import org.oddjob.ConsoleCapture;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.io.BufferType;
import org.oddjob.io.StdoutType;
import org.oddjob.jobs.BeanReportJob;
import org.oddjob.state.ParentState;

public class SQLJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(SQLJobTest.class);

	public void testSql() throws Exception {
		
		ConnectionType ct = new ConnectionType();
		ct.setDriver("org.hsqldb.jdbcDriver");
		ct.setUrl("jdbc:hsqldb:mem:test");
		ct.setUsername("sa");
		ct.setPassword("");
		
		SQLJob test = new SQLJob();
		
		BufferType buffer = new BufferType();
		buffer.setText("drop table TEST if exists");		
		buffer.configured();
		
		SQLResultsBean beans = new SQLResultsBean();
		test.setResults(beans);
		
		test.setConnection(ct.toValue());
		test.setInput(buffer.toInputStream());
		test.setArooaSession(new StandardArooaSession());
		test.run();

		buffer.setText("create table TEST(greeting VARCHAR(20))");
		buffer.configured();
		
		test.setConnection(ct.toValue());
		test.setInput(buffer.toInputStream());
		test.run();
		
		buffer.setText("insert into TEST values ('hello')");
		buffer.configured();
		
		test.setConnection(ct.toValue());
		test.setInput(buffer.toInputStream());
		
		test.run();
		logger.debug("Inserted: " + beans.getUpdateCount());
		
		buffer.setText("insert into TEST values ('goodbye')");
		buffer.configured();
		
		test.setConnection(ct.toValue());
		test.setInput(buffer.toInputStream());
		test.run();
		
		buffer.setText("select * from TEST");
		buffer.configured();
		
		test.setConnection(ct.toValue());
		test.setInput(buffer.toInputStream());
		test.run();
		
		assertEquals("hello", ((DynaBean) beans.getRows()[0]).get("GREETING"));
		assertEquals("goodbye", ((DynaBean) beans.getRows()[1]).get("GREETING"));	
		
		buffer.setText("shutdown");
		buffer.configured();
		
		test.setConnection(ct.toValue());
		test.setInput(buffer.toInputStream());
		test.run();
		
		assertEquals(null, beans.getRows());
	}
		
	public void testInOddjob() throws Exception {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("Resource",
				getClass().getResourceAsStream("SqlJobTest.xml")));

		oj.run();

		List<?> o = new OddjobLookup(oj).lookup(
				"result.rows", List.class);

		BeanReportJob rep = new BeanReportJob();
		rep.setOutput(new StdoutType().toValue());
		rep.setArooaSession(new StandardArooaSession());
		rep.setBeans(o);
		
		rep.run();
		
		Integer result = new OddjobLookup(oj).lookup(
				"result.row.count", Integer.class);
		
		assertEquals(new Integer(10), result);
		
		oj.destroy();
	}
	
	public void testFirstExample() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SQLFirstExample.xml",
				getClass().getClassLoader()));

		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		oj.run();

		console.close();
		console.dump(logger);
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		
		String[] lines = console.getLines();
		
		assertEquals(2, lines.length);
		
		assertEquals("Hello", lines[0].trim());
		assertEquals("Hello", lines[1].trim());
		
		Connection connection = new OddjobLookup(oj).lookup(
				"connection", Connection.class);
		
		SQLJob shutdown = new SQLJob();
		shutdown.setArooaSession(new StandardArooaSession());
		shutdown.setConnection(connection);
		shutdown.setInput(new ByteArrayInputStream("shutdown".getBytes()));
		shutdown.run();
	}
	
	public void testInOddjobEmptyResultSet() throws Exception {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("Resource",
				getClass().getResourceAsStream("SqlJobTestEmptyResultSet.xml")));

		oj.run();

		OddjobLookup lookup = new OddjobLookup(oj);
		
		List<?> o = lookup.lookup(
				"result.rows", List.class);

		assertNotNull(o);
		
		int rows = lookup.lookup(
				"result.rowCount", int.class);
		
		assertEquals(0, rows);
		
		oj.destroy();
	}
	
	public void testMultipleStatements() throws Exception {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("Resource",
				getClass().getResourceAsStream("SqlJobMultipleStmtsTest.xml")));

		oj.run();

		OddjobLookup lookup = new OddjobLookup(oj);
		
		assertEquals(new Integer(1), lookup.lookup(
				"result.rowSetCount", Integer.class));
		
		String [] rows = lookup.lookup(
				"result.rows", String[].class);

		assertEquals(6, rows.length);
		assertEquals("01", lookup.lookup(
				"result.rows[0].NUMBER", String.class));
		assertEquals("06", lookup.lookup(
				"result.rows[5].NUMBER", String.class));
				
		oj.destroy();
	}
	
	public void testContinueOnFailure() throws Exception {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("Resource",
				getClass().getResourceAsStream("SqlJobContinueOnFailureTest.xml")));
		oj.setArgs(new String[] { "CONTINUE" });
		oj.run();

		OddjobLookup lookup = new OddjobLookup(oj);
		
		assertEquals(new Integer(1), lookup.lookup(
				"result.rowSetCount", Integer.class));
		
		String [] rows = lookup.lookup(
				"result.rows", String[].class);

		assertEquals(5, rows.length);
		assertEquals("01", lookup.lookup(
				"result.rows[0].NUMBER", String.class));
		assertEquals("06", lookup.lookup(
				"result.rows[4].NUMBER", String.class));
				
		assertEquals(9, lookup.lookup("sql-job.executedSQLCount"));
		assertEquals(8, lookup.lookup("sql-job.successfulSQLCount"));
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		
		oj.destroy();
	}
	
	public void testStopOnFailure() throws Exception {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("Resource",
				getClass().getResourceAsStream("SqlJobContinueOnFailureTest.xml")));
		oj.setArgs(new String[] { "STOP" });
		oj.run();

		OddjobLookup lookup = new OddjobLookup(oj);
		
		assertEquals(new Integer(0), lookup.lookup(
				"result.rowSetCount", Integer.class));
		
		Connection connection = lookup.lookup("connection", Connection.class);
		try {
			connection.createStatement().execute("shutdown");
		}
		finally {
			connection.close();
		}
		
		assertEquals(3, lookup.lookup("sql-job.executedSQLCount"));
		assertEquals(2, lookup.lookup("sql-job.successfulSQLCount"));
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		
		oj.destroy();
	}
	
	public void testTerminateOnFailure() throws Exception {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("Resource",
				getClass().getResourceAsStream("SqlJobContinueOnFailureTest.xml")));
		oj.setArgs(new String[] { "ABORT" });
		oj.run();

		OddjobLookup lookup = new OddjobLookup(oj);
		
		assertEquals(new Integer(0), lookup.lookup(
				"result.rowSetCount", Integer.class));
		
		Connection connection = lookup.lookup("connection", Connection.class);
		try {
			connection.createStatement().execute("shutdown");
		}
		finally {
			connection.close();
		}
		
		assertEquals(3, lookup.lookup("sql-job.executedSQLCount"));
		assertEquals(2, lookup.lookup("sql-job.successfulSQLCount"));
		
		assertEquals(ParentState.EXCEPTION, oj.lastStateEvent().getState());
		
		oj.destroy();
	}
}
