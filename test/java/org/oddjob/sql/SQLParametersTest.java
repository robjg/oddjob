package org.oddjob.sql;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

public class SQLParametersTest extends TestCase {

	public void testSomeInserts() throws SQLException, ArooaPropertyException, ArooaConversionException {		
		
		ConnectionType connection = new ConnectionType();
		connection.setDriver("org.hsqldb.jdbc.JDBCDriver");
		connection.setUrl("jdbc:hsqldb:mem:testdb;shutdown=true");
		connection.setUsername("sa");

		Connection keepAlive = connection.toValue();
		
		String xml =
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <sql>" +
			"	  <connection>" +
			"      <value value='${c}'/>" +
			"     </connection>" +
			"     <input>" +
			"      <buffer>" +
			"create table TEST (fruit varchar(20), quantity int)" +
			"      </buffer>" +
			"     </input>" +
			"    </sql>" +
			"    <sql id='first'>" +
			"	  <connection>" +
			"      <value value='${c}'/>" +
			"     </connection>" +
			"     <parameters>" +
			"        <value value='apples'/>" +
			"        <value value='27'/>" +
			"     </parameters>" +
			"     <input>" +
			"      <buffer id='insert-sql'>" +
			"insert into TEST values (?, ?)" +
			"      </buffer>" +
			"     </input>" +
			"    </sql>" +
			"    <sql>" +
			"	  <connection>" +
			"      <value value='${c}'/>" +
			"     </connection>" +
			"     <parameters>" +
			"        <value value='oranges'/>" +
			"        <value value='52'/>" +
			"     </parameters>" +
			"     <input>" +
			"      <buffer>" +
			"${insert-sql}" +
			"      </buffer>" +
			"     </input>" +
			"    </sql>" +
			"    <sql>" +
			"	  <connection>" +
			"      <value value='${c}'/>" +
			"     </connection>" +
			"     <results>" +
			"      <sql-results-bean id='result'/>" +
			"     </results>" +
			"     <input>" +
			"      <buffer>" +
			"select count(*) as c from TEST" +
			"      </buffer>" +
			"     </input>" +
			"    </sql>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("TEST", xml));
		oddjob.setExport("c", connection);

		oddjob.run();
		
		assertEquals(JobState.COMPLETE, oddjob.lastJobStateEvent().getJobState());
		
		int count = new OddjobLookup(oddjob).lookup("result.row.C", Integer.class);
		
		assertEquals(2, count);
		
		keepAlive.close();
	}
	
	public void testInsertsMultipleStatements() throws SQLException, ArooaPropertyException, ArooaConversionException {		
		
		ConnectionType connection = new ConnectionType();
		connection.setDriver("org.hsqldb.jdbc.JDBCDriver");
		connection.setUrl("jdbc:hsqldb:mem:testdb");
		connection.setUsername("sa");

		String xml =
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <sql>" +
			"	  <connection>" +
			"      <value value='${c}'/>" +
			"     </connection>" +
			"     <parameters>" +
			"        <value value='apples'/>" +
			"        <value value='2'/>" +
			"     </parameters>" +
			"     <input>" +
			"      <buffer>" +
			"create table TEST (id int, fruit varchar(20), quantity int);\n" +
			"insert into TEST values (1, ?, ?);\n" +
			"insert into TEST values (2, ?, ?);\n" +
			"insert into TEST values (3, ?, 4);\n" +
			"select count(*) as c, sum(quantity) as s from TEST;\n" +
			"shutdown" +
			"      </buffer>" +
			"     </input>" +
			"     <results>" +
			"      <sql-results-bean id='result'/>" +
			"     </results>" +
			"    </sql>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("TEST", xml));
		oddjob.setExport("c", connection);

		oddjob.run();
		
		assertEquals(JobState.COMPLETE, oddjob.lastJobStateEvent().getJobState());
		
		int count = new OddjobLookup(oddjob).lookup("result.row.C", Integer.class);
		
		assertEquals(3, count);
		
		int sum = new OddjobLookup(oddjob).lookup("result.row.S", Integer.class);
		
		assertEquals(8, sum);
	}
	
	/**
	 * Test making things callable. It would be nice to test out params, but H2 doesn't
	 * support that.
	 * @throws SQLException
	 * @throws ArooaPropertyException
	 * @throws ArooaConversionException
	 */
	public void testCallable() throws SQLException, ArooaPropertyException, ArooaConversionException {		
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SQLCallableStatement.xml", 
				getClass().getClassLoader()));

		oddjob.run();
		
		assertEquals(JobState.COMPLETE, oddjob.lastJobStateEvent().getJobState());
		
		int a = new OddjobLookup(oddjob).lookup("a", Integer.class);
		int b = new OddjobLookup(oddjob).lookup("b", Integer.class);
		
		assertEquals(2, a);
		assertEquals(3, b);
		
		Connection connection = new OddjobLookup(oddjob).lookup(
				"connection", Connection.class);
		
		SQLJob shutdown = new SQLJob();
		shutdown.setArooaSession(new StandardArooaSession());
		shutdown.setConnection(connection);
		shutdown.setInput(new ByteArrayInputStream("shutdown".getBytes()));
		shutdown.run();
	}
}
