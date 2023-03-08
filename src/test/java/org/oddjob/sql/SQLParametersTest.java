package org.oddjob.sql;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class SQLParametersTest extends OjTestCase {

    @Test
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
                        "      <buffer>" +
                        "${insert-sql}" +
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
                        "    <sql id='query'>" +
                        "	  <connection>" +
                        "      <value value='${c}'/>" +
                        "     </connection>" +
                        "     <results>" +
                        "      <sql-results-bean/>" +
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
        oddjob.setExport("insert-sql", new ArooaObject("insert into TEST values (?, ?)"));
        oddjob.run();

        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

        int count = new OddjobLookup(oddjob).lookup("query.results.row.C", Integer.class);

        assertEquals(2, count);

        keepAlive.close();

        oddjob.destroy();
    }

    @Test
    public void testInsertsMultipleStatements() throws ArooaPropertyException, ArooaConversionException {

        ConnectionType connection = new ConnectionType();
        connection.setDriver("org.hsqldb.jdbc.JDBCDriver");
        connection.setUrl("jdbc:hsqldb:mem:testdb");
        connection.setUsername("sa");

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <sql id='sql'>" +
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
                        "      <sql-results-bean/>" +
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

        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

        int count = new OddjobLookup(oddjob).lookup("sql.results.row.C", Integer.class);

        assertEquals(3, count);

        int sum = new OddjobLookup(oddjob).lookup("sql.results.row.S", Integer.class);

        assertEquals(8, sum);

        oddjob.destroy();
    }

    /**
     * Test making things callable. It would be nice to test out params, but H2 doesn't
     * support that.
     *
     * @throws ArooaPropertyException
     * @throws ArooaConversionException
     */
    @Test
    public void testCallable() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/sql/SQLCallableStatement.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Integer a = lookup.lookup("a", Integer.class);
        Integer b = lookup.lookup("b", Integer.class);

        assertEquals(Integer.valueOf(2), a);
        assertEquals(Integer.valueOf(3), b);

        a = lookup.lookup("sql-call.parameters[0]", Integer.class);
        b = lookup.lookup("sql-call.parameters[1]", Integer.class);

        // Why - This is a bug in the RunnableWRapper!
        assertNull(a);
        assertNull(b);

        Connection connection = new OddjobLookup(oddjob).lookup(
                "vars.connection", Connection.class);

        SQLJob shutdown = new SQLJob();
        shutdown.setArooaSession(new StandardArooaSession());
        shutdown.setConnection(connection);
        shutdown.setInput(new ByteArrayInputStream("shutdown".getBytes()));
        shutdown.run();

        oddjob.destroy();
    }

//	public void testHSQLAssumptionsReMultipleResultSets() throws ArooaConversionException, SQLException {
//		
//		ConnectionType connectionType = new ConnectionType();
//		connectionType.setDriver("org.hsqldb.jdbc.JDBCDriver");
//		connectionType.setUrl("jdbc:hsqldb:mem:testdb;shutdown=true");
//		connectionType.setUsername("sa");
//
//		Connection connection = connectionType.toValue();
//		
//		Statement setupStmt = connection.createStatement();
//		setupStmt.executeUpdate("");
//		// TOOD: - finish.
//		
//	}
}
