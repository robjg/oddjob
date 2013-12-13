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
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.SimpleBusService;
import org.oddjob.io.BufferType;
import org.oddjob.io.StdoutType;
import org.oddjob.jobs.BeanReportJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;

public class SQLJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(SQLJobTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("---------------  " + getName() + "  ----------------");
	}
	
	public void testSql() throws Exception {
		
		ArooaSession session = new StandardArooaSession();
		
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
		beans.setBusConductor(test.getServices(
				).getService(SimpleBusService.BEAN_BUS_SERVICE_NAME));
		test.setResults(beans);
		
		test.setConnection(ct.toValue());
		test.setInput(buffer.toInputStream());
		test.setArooaSession(session);
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
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
						"org/oddjob/sql/SqlJobTest.xml",
						getClass().getClassLoader()));

		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());

		List<?> o = new OddjobLookup(oddjob).lookup(
				"query.results.rows", List.class);

		BeanReportJob rep = new BeanReportJob();
		rep.setOutput(new StdoutType().toValue());
		rep.setArooaSession(new StandardArooaSession());
		rep.setBeans(o);
		
		rep.run();
		
		Integer result = new OddjobLookup(oddjob).lookup(
				"query.results.row.count", Integer.class);
		
		assertEquals(new Integer(10), result);
		
		oddjob.destroy();
	}
	
	public void testFirstExample() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SQLFirstExample.xml",
				getClass().getClassLoader()));

		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		oddjob.run();

		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.close();
		console.dump(logger);
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		String[] lines = console.getLines();
		
		assertEquals(2, lines.length);
		
		assertEquals("Hello", lines[0].trim());
		assertEquals("Hello", lines[1].trim());
		
		// Run query twice - check bug where results were doubling up.
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		Object query = lookup.lookup("query");
		((Resetable) query).hardReset();
		assertEquals(JobState.READY, 
				((Stateful) query).lastStateEvent().getState());
		((Runnable) query).run();
		assertEquals(JobState.COMPLETE, 
				((Stateful) query).lastStateEvent().getState());
		
		assertEquals("Hello", lookup.lookup("query.results.row.TEXT"));
		
		// Shutdown
		
		Connection connection = new OddjobLookup(oddjob).lookup(
				"vars.connection", Connection.class);
		
		SQLJob shutdown = new SQLJob();
		shutdown.setArooaSession(new StandardArooaSession());
		shutdown.setConnection(connection);
		shutdown.setInput(new ByteArrayInputStream("shutdown".getBytes()));
		shutdown.run();
		
		oddjob.destroy();
	}
	
	public void testInOddjobEmptyResultSet() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SqlJobTestEmptyResultSet.xml",
				getClass().getClassLoader()));

		oddjob.run();

		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		List<?> o = lookup.lookup(
				"query.results.rows", List.class);

		assertNotNull(o);
		
		int rows = lookup.lookup(
				"query.results.rowCount", int.class);
		
		assertEquals(0, rows);
		
		oddjob.destroy();
	}
	
	public void testMultipleStatements() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SqlJobMultipleStmtsTest.xml",
				getClass().getClassLoader()));

		oddjob.run();

		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals(new Integer(1), lookup.lookup(
				"sql.results.rowSetCount", Integer.class));
		
		String [] rows = lookup.lookup(
				"sql.results.rows", String[].class);

		assertEquals(6, rows.length);
		assertEquals("01", lookup.lookup(
				"sql.results.rows[0].NUMBER", String.class));
		assertEquals("06", lookup.lookup(
				"sql.results.rows[5].NUMBER", String.class));
				
		oddjob.destroy();
	}
	
	public void testContinueOnFailure() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SqlJobContinueOnFailureTest.xml",
				getClass().getClassLoader()));
		
		oddjob.setArgs(new String[] { "CONTINUE", "false" });
		oddjob.run();

		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals(new Integer(1), lookup.lookup(
				"sql-query.results.rowSetCount", Integer.class));
		
		String [] rows = lookup.lookup(
				"sql-query.results.rows", String[].class);

		assertEquals(5, rows.length);
		assertEquals("01", lookup.lookup(
				"sql-query.results.rows[0].NUMBER", String.class));
		assertEquals("06", lookup.lookup(
				"sql-query.results.rows[4].NUMBER", String.class));
				
		assertEquals(7, lookup.lookup("sql-job.executedSQLCount"));
		assertEquals(6, lookup.lookup("sql-job.successfulSQLCount"));
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
	
	public void testStopOnFailure() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SqlJobContinueOnFailureTest.xml",
				getClass().getClassLoader()));
		
		oddjob.setArgs(new String[] { "STOP", "true" });
		oddjob.run();

		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());

		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals(new Integer(1), lookup.lookup(
				"sql-query.results.rowSetCount", Integer.class));
		
		String [] rows = lookup.lookup(
				"sql-query.results.rows", String[].class);

		assertEquals(1, rows.length);
		assertEquals("01", lookup.lookup(
				"sql-query.results.rows[0].NUMBER", String.class));
				
		assertEquals(3, lookup.lookup("sql-job.executedSQLCount"));
		assertEquals(2, lookup.lookup("sql-job.successfulSQLCount"));
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
	
	public void testAbortOnFailure() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SqlJobContinueOnFailureTest.xml",
				getClass().getClassLoader()));
		oddjob.setArgs(new String[] { "ABORT", "false" });
		oddjob.run();

		assertEquals(ParentState.EXCEPTION, 
				oddjob.lastStateEvent().getState());

		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals(new Integer(1), lookup.lookup(
				"sql-query.results.rowSetCount", Integer.class));
		
		String [] rows = lookup.lookup(
				"sql-query.results.rows", String[].class);

		assertEquals(0, rows.length);
		
		assertEquals(3, lookup.lookup("sql-job.executedSQLCount"));
		assertEquals(2, lookup.lookup("sql-job.successfulSQLCount"));
		
		assertEquals(ParentState.EXCEPTION, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
	
	public void testAbortOnFailureWithAutocommit() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SqlJobContinueOnFailureTest.xml",
				getClass().getClassLoader()));
		oddjob.setArgs(new String[] { "ABORT", "true" });
		oddjob.run();

		assertEquals(ParentState.EXCEPTION, 
				oddjob.lastStateEvent().getState());

		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals(new Integer(1), lookup.lookup(
				"sql-query.results.rowSetCount", Integer.class));
		
		String [] rows = lookup.lookup(
				"sql-query.results.rows", String[].class);

		assertEquals(1, rows.length);
		assertEquals("01", lookup.lookup(
				"sql-query.results.rows[0].NUMBER", String.class));
		
		assertEquals(3, lookup.lookup("sql-job.executedSQLCount"));
		assertEquals(2, lookup.lookup("sql-job.successfulSQLCount"));
		
		assertEquals(ParentState.EXCEPTION, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
}
