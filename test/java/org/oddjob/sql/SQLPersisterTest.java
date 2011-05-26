/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.sql;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;

import junit.framework.TestCase;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.WaitJob;
import org.oddjob.state.JobState;

public class SQLPersisterTest extends TestCase {
	private static final Logger logger = Logger.getLogger(SQLPersisterTest.class);
	
	public static class Sample implements Serializable {
		private static final long serialVersionUID = 2006111;
		String value;
	}
	
	public void testSql() throws Exception {
		Oddjob setUp = new Oddjob();
		setUp.setConfiguration(new XMLConfiguration("Resource",
				getClass().getResourceAsStream("create.xml")));
		setUp.run();
		assertEquals(JobState.COMPLETE, setUp.lastJobStateEvent().getJobState());

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
		
	public void testInOddjob() throws Exception {		
		
		Oddjob setUp = new Oddjob();
		setUp.setConfiguration(new XMLConfiguration("Resource",
				getClass().getResourceAsStream("create.xml")));
		setUp.run();
		assertEquals(JobState.COMPLETE, setUp.lastJobStateEvent().getJobState());
		
    	URL url = getClass().getClassLoader().getResource("org/oddjob/sql/SqlPersisterTest.xml");
    	
    	File file = new File(url.getFile());
    	        

		
		Oddjob oj = new Oddjob();
		oj.setFile(file);
		oj.run();
	
//		OddjobExplorer explorer = new OddjobExplorer();
//		explorer.setOddjob(oj);
//		explorer.run();
//		
		WaitJob wait = new WaitJob();
		wait.setFor(oj);
		wait.setState("COMPLETE");
		wait.run();
		
		Object echoJob = new OddjobLookup(oj).lookup("oj/e"); 
		
		assertEquals(JobState.COMPLETE, oj.lastJobStateEvent().getJobState());
		assertEquals("0009", PropertyUtils.getProperty(
				echoJob, "text"));
		
		oj.destroy();
		
		Oddjob oj2 = new Oddjob();
		oj2.setFile(file);
		oj2.hardReset();
		oj2.run();
		
		WaitJob wait2 = new WaitJob();
		wait2.setFor(oj2);
		wait2.setState("COMPLETE");
		wait2.run();
		
		assertEquals("0019", PropertyUtils.getProperty(
				new OddjobLookup(oj2).lookup("oj/e"), "text"));
		
		oj2.destroy();
		
		new OddjobLookup(setUp).lookup("vars.con", 
				Connection.class).createStatement().execute("shutdown");
	}
}
