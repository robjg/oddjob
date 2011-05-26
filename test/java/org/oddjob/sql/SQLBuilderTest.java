package org.oddjob.sql;

import junit.framework.TestCase;

public class SQLBuilderTest extends TestCase {
		
	String EOL = System.getProperty("line.separator");
	
	public void testLineFeedTest() {
		
		String sql = 
			"create" + EOL + 
			"table" + EOL +
			"test";
		
		SQLBuilder test= new SQLBuilder();
		
		test.append(sql);
		
		assertEquals("create table test", test.toString());
	}
	
	
//  JDBC doesn't deal with line feeds (or at least hsql doesn't).	
//	public void testLineFeedInSql() throws SQLException {
//		
//		ConnectionType ct = new ConnectionType();
//		ct.setDriver("org.hsqldb.jdbcDriver");
//		ct.setUrl("jdbc:hsqldb:mem:test");
//		ct.setUsername("sa");
//		ct.setPassword("");
//
////		String sql = 
////			"create" + EOL + 
////			"table" + EOL +
////			"test";
//
//		String sql = "create table test1" + EOL +
//					"create table test1";
//		
//		Connection connection  = ct.toValue();
//		
//		Statement stmt = connection.createStatement();
//		
//		try {
//			stmt.execute(sql);
//		} 
//		finally {
//			stmt.execute("shutdown");
//		}
//		
//		connection.close();
//	}
	
}
