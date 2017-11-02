/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.sql;

import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;

import org.oddjob.OjTestCase;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

public class SQLPersisterTest extends OjTestCase {
	private static final Logger logger = Logger.getLogger(SQLPersisterTest.class);
	
	public static class Sample implements Serializable {
		private static final long serialVersionUID = 2006111;
		String value;
	}
	
   @Test
	public void testSql() throws Exception {

		Oddjob setUp = new Oddjob();
		
		setUp.setConfiguration(new XMLConfiguration("Resource",
				getClass().getResourceAsStream("create.xml")));
		setUp.run();
		
		assertEquals(ParentState.COMPLETE, setUp.lastStateEvent().getState());

		ConnectionType connection = new OddjobLookup(
				setUp).lookup("vars.con", ConnectionType.class);
		
		SQLPersisterService test = new SQLPersisterService();
		test.setConnection(connection.toValue());
		test.start();
		
		Sample sample = new Sample();
		
		String text = "This is a deliberately long bit of text.";  
		sample.value = text;

		StandardArooaSession session = new StandardArooaSession();
		
		ComponentPersister persister = 
			test.getPersister("test").persisterFor("oj");
		
		persister.persist("foo", sample, session);
		
		Object o = persister.restore("foo", getClass().getClassLoader(), session);
		assertNotNull(o);
				
		assertEquals(Sample.class, o.getClass());
		Sample copy = (Sample) o;
		logger.debug(copy.value);
		assertEquals(text, copy.value);
		
		test.stop();
		
		Connection c = connection.toValue();
		c.createStatement().execute("shutdown");
		c.close();
	}
		
   @Test
	public void testInOddjob() throws Exception {		
		
		Oddjob setUp = new Oddjob();
		
		setUp.setConfiguration(new XMLConfiguration("Resource",
				getClass().getResourceAsStream("create.xml")));
		setUp.run();
		
		assertEquals(ParentState.COMPLETE, setUp.lastStateEvent().getState());
		
    	URL url = getClass().getClassLoader().getResource("org/oddjob/sql/SqlPersisterTest.xml");
    	
    	File file = new File(url.toURI());
    	        		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		StateSteps oddjobState = new StateSteps(oddjob);
		oddjobState.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.COMPLETE);
		
		oddjob.run();
	
//		OddjobExplorer explorer = new OddjobExplorer();
//		explorer.setOddjob(oj);
//		explorer.run();
//		
		oddjobState.checkNow();
		
		Object echoJob = new OddjobLookup(oddjob).lookup("oj/e"); 
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		assertEquals("0009", PropertyUtils.getProperty(
				echoJob, "text"));
		
		oddjob.destroy();
		
		Oddjob oj2 = new Oddjob();
		
		StateSteps oddjobState2 = new StateSteps(oj2);
		oddjobState2.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.COMPLETE);
		
		oj2.setFile(file);
		oj2.hardReset();
		oj2.run();
		
		oddjobState2.checkNow();
		
		assertEquals("0019", PropertyUtils.getProperty(
				new OddjobLookup(oj2).lookup("oj/e"), "text"));
		
		oj2.destroy();
		
		new OddjobLookup(setUp).lookup("vars.con", 
				Connection.class).createStatement().execute("shutdown");
	}
}
