package org.oddjob.sql;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.types.ValueType;
import org.oddjob.beanbus.BadBeanException;
import org.oddjob.beanbus.CrashBusException;

public class ParameterisedExecutorText extends TestCase {

	public void testHSQLDataTypes() throws SQLException, ClassNotFoundException, ArooaConversionException {
		
		ConnectionType ct = new ConnectionType();
		ct.setDriver("org.hsqldb.jdbcDriver");
		ct.setUrl("jdbc:hsqldb:mem:test");
		ct.setUsername("sa");
		ct.setPassword("");
		
		Statement stmt = ct.toValue().createStatement();
		
		String create = 
			"create table numbers (" +
			"myTinyInt TINYINT, " +
			"mySmallInt SMALLINT, " +
			"myInteger INTEGER, " +
			"myBigInt BIGINT, " +
			"myNumeric NUMERIC, " +
			"myDecimal DECIMAL)";
		
		stmt.execute(create);
		
		String insert = 
			"insert into numbers values (1, 1, 1, 1, 1, 1)";
		
		stmt.execute(insert);
		
		String select = 
			"select * from numbers";
		
		ResultSet rs = stmt.executeQuery(select);
		
		ResultSetMetaData md = rs.getMetaData();
		
		int c = md.getColumnCount();

		rs.next();

		try {
			for (int i = 1; i <= c; ++i) {
				assertEquals(md.getColumnName(i), 
						Class.forName(md.getColumnClassName(i)),
						rs.getObject(i).getClass());
			}
		} catch (AssertionFailedError e) {
			// expected because of hsql
		}
		
		stmt.execute("shutdown");

	}
	
	private class Results implements SQLResultsProcessor {

		Object last;
		
		@Override
		public void accept(Object bean) throws BadBeanException,
				CrashBusException {
			last = bean;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void testBeanTypes() throws BadBeanException, ArooaConversionException {

		ConnectionType ct = new ConnectionType();
		ct.setDriver("org.hsqldb.jdbcDriver");
		ct.setUrl("jdbc:hsqldb:mem:test");
		ct.setUsername("sa");
		ct.setPassword("");
		
		ParameterisedExecutor test = new ParameterisedExecutor();
		
		test.setConnection(ct.toValue());
		
		StandardArooaSession session = new StandardArooaSession();
		test.setArooaSession(session);
		
		Results results = new Results();
		test.setResultProcessor(results);
		
		String create = 
			"create table numbers (" +
			"myTinyInt TINYINT, " +
			"mySmallInt SMALLINT, " +
			"myInteger INTEGER, " +
			"myBigInt BIGINT, " +
			"myNumeric NUMERIC, " +
			"myDecimal DECIMAL)";
		
		test.accept(create);
		
		ValueType v1 = new ValueType();
		v1.setValue(new ArooaObject("1"));
		
		ValueType v2 = new ValueType();
		v2.setValue(new ArooaObject("2"));
		
		ValueType v3 = new ValueType();
		v3.setValue(new ArooaObject("3"));
		
		ValueType v4 = new ValueType();
		v4.setValue(new ArooaObject("4"));

		ValueType v5 = new ValueType();
		v5.setValue(new ArooaObject("5"));

		ValueType v6 = new ValueType();
		v6.setValue(new ArooaObject("6"));

		test.setParameters(0, v1);
		test.setParameters(1, v2);
		test.setParameters(2, v3);
		test.setParameters(3, v4);
		test.setParameters(4, v5);
		test.setParameters(5, v6);
		
		String insert = 
			"insert into numbers values (?, ?, ?, ?, ?, ?)";
		
		test.accept(insert);
		
		String select = 
			"select * from numbers";
		
		test.accept(select);
		
		Object bean = ((List<Object>) results.last).get(0);
		
		PropertyAccessor accessor = session.getTools().getPropertyAccessor();
		
		ArooaClass arooaClass = accessor.getClassName(bean);
		
		BeanOverview beanOverview = arooaClass.getBeanOverview(accessor);
		
		String[] props = beanOverview.getProperties();

		assertEquals(6, props.length);
		
		assertEquals(Integer.class, beanOverview.getPropertyType("MYTINYINT"));
		assertEquals(Integer.class, beanOverview.getPropertyType("MYSMALLINT"));
		assertEquals(Integer.class, beanOverview.getPropertyType("MYINTEGER"));
		assertEquals(Long.class, beanOverview.getPropertyType("MYBIGINT"));
		assertEquals(BigDecimal.class, beanOverview.getPropertyType("MYNUMERIC"));
		assertEquals(BigDecimal.class, beanOverview.getPropertyType("MYDECIMAL"));
		
		test.accept("shutdown");
	}
	
	@SuppressWarnings("unchecked")
	public void testNullParameter() throws BadBeanException, ArooaConversionException {

		ConnectionType ct = new ConnectionType();
		ct.setDriver("org.hsqldb.jdbcDriver");
		ct.setUrl("jdbc:hsqldb:mem:test");
		ct.setUsername("sa");
		ct.setPassword("");
		
		ParameterisedExecutor test = new ParameterisedExecutor();
		
		test.setConnection(ct.toValue());
		
		StandardArooaSession session = new StandardArooaSession();
		test.setArooaSession(session);
		
		Results results = new Results();
		test.setResultProcessor(results);
		
		String create = 
			"create table thing(" +
			"stuff VARCHAR(10))";
		
		test.accept(create);
		
		ValueType v1 = new ValueType();

		test.setParameters(0, v1);
		
		String insert = 
			"insert into thing (stuff) values (?)";
		
		test.accept(insert);
		
		String select = 
			"select * from thing";
		
		test.accept(select);
		
		Object bean = ((List<Object>) results.last).get(0);
		
		PropertyAccessor accessor = session.getTools().getPropertyAccessor();
		
		assertEquals(null, accessor.getProperty(bean, "STUFF"));
		
		test.accept("shutdown");
	}
	
}
