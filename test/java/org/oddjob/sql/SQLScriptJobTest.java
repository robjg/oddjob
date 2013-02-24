/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.sql;

import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.BusService;
import org.oddjob.io.BufferType;
import org.oddjob.io.StdoutType;
import org.oddjob.state.ParentState;

public class SQLScriptJobTest extends TestCase {

	private static final Logger logger = Logger.getLogger(SQLScriptJobTest.class);
	
	protected void setUp() throws Exception {
		logger.info("-----------------  " + getName() + "  --------------------");
	}
	
	String EOL = System.getProperty("line.separator");
	
	public void testSql() throws Exception {
		
		ArooaSession session = new StandardArooaSession();
		
		ConnectionType ct = new ConnectionType();
		ct.setDriver("org.hsqldb.jdbcDriver");
		ct.setUrl("jdbc:hsqldb:mem:test");
		ct.setUsername("sa");
		ct.setPassword("");
		
		BufferType buffer = new BufferType();
		buffer.setText("drop table TEST if exists;" + EOL +
				"create table TEST(greeting VARCHAR(20));" + EOL +
				"insert into TEST values ('hello');" + EOL +
				"insert into TEST values ('goodbye');");
		buffer.configured();
		
		SQLJob test = new SQLJob();
		test.setConnection(ct.toValue());
		test.setInput(buffer.toInputStream());
		test.setArooaSession(session);
		test.run();
		
		SQLJob results = new SQLJob();
		
		SQLResultsBean beans = new SQLResultsBean();
		beans.setArooaSession(session);
		beans.setBusConductor(results.getServices(
				).getService(BusService.BEAN_BUS_SERVICE_NAME));
		
		results.setResults(beans);
		
		results.setArooaSession(session);
		results.setConnection(ct.toValue());
		
		buffer.setText("select * from TEST");
		buffer.configured();
		
		results.setInput(buffer.toInputStream());
		results.run();
		
		assertEquals("hello", ((DynaBean) beans.getRows()[0]).get("GREETING"));
		assertEquals("goodbye", ((DynaBean) beans.getRows()[1]).get("GREETING"));	
	}
	
	
	public void testInOddjob() throws Exception {
		
		ArooaSession session = new StandardArooaSession();
		
		ConnectionType ct = new ConnectionType();
		ct.setDriver("org.hsqldb.jdbcDriver");
		ct.setUrl("jdbc:hsqldb:mem:test");
		ct.setUsername("sa");
		ct.setPassword("");
		
		SQLJob test;
		
		BufferType buffer = new BufferType();
		buffer.setText("create table NUMBERS(NUMBER varchar(20))");
		buffer.configured();
		
		test = new SQLJob();
		test.setConnection(ct.toValue());
		test.setInput(buffer.toInputStream());
		test.setArooaSession(session);
		test.run();
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("Resource",
				getClass().getResourceAsStream("SqlScriptJobTest.xml")));

		oj.run();
		
		test = new SQLJob();
		test.setConnection(ct.toValue());
		
		buffer.setText("select count(*) \"count\" from numbers;\n" +
				"shutdown");
		buffer.configured();
		
		test.setInput(buffer.toInputStream());
		
		SQLResultsBean beans = new SQLResultsBean();
		beans.setArooaSession(session);
		beans.setBusConductor(test.getServices(
				).getService(BusService.BEAN_BUS_SERVICE_NAME));
		
		test.setResults(beans);
		test.setArooaSession(session);

		test.run();
		
		assertEquals(new Long(10), PropertyUtils.getProperty(
				test, "results.row.count"));
	}
	
	public void testSqlResultsSheet() throws Exception {

		StdoutType out = new StdoutType();
		
		InputStream input = 
			getClass().getResourceAsStream("SqlScriptJobSheetTest.xml");
		
		assertNotNull(input);
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("Resource", input));
		oddjob.setExport("output", out);
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
}
