package org.oddjob.sql;
import org.junit.Before;
import org.junit.After;

import org.junit.Test;

import java.sql.SQLException;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.io.BufferType;
import org.oddjob.persist.OddjobPersister;
import org.oddjob.persist.SilhouetteFactory;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;

public class SQLSilhouettesServiceTest extends OjTestCase {

	private static final Logger logger = 
		LoggerFactory.getLogger(SQLSilhouettesServiceTest.class);
	
	ConnectionType ct;
		
    @Before
    public void setUp() throws Exception {
		logger.debug("------------------------- " + getName() + " ---------------------");
		
		ct = new ConnectionType();
		ct.setDriver("org.hsqldb.jdbcDriver");
		ct.setUrl("jdbc:hsqldb:mem:test");
		ct.setUsername("sa");
		ct.setPassword("");
		
		BufferType buffer = new BufferType();
		buffer.setText("CREATE TABLE oddjob(" +
				"path VARCHAR(128), " +
				"id VARCHAR(32), " +
				"job BLOB, " +
				"CONSTRAINT oddjob_pk PRIMARY KEY (path, id))");
		buffer.configured();

		SQLJob sql = new SQLJob();
		sql.setArooaSession(new StandardArooaSession());
		sql.setInput(buffer.toInputStream());
		
		sql.setConnection(ct.toValue());
		
		sql.run();
	}
	
    @After
    public void tearDown() throws Exception {
		
		BufferType buffer = new BufferType();
		buffer.setText("shutdown");
		buffer.configured();

		SQLJob sql = new SQLJob();
		sql.setArooaSession(new StandardArooaSession());
		sql.setInput(buffer.toInputStream());
		
		sql.setConnection(ct.toValue());
		
		sql.run();
	}
	
	public static class SessionCapture implements ArooaSessionAware {

		ArooaSession arooaSession;
		
		@Override
		public void setArooaSession(ArooaSession session) {
			this.arooaSession = session;
		}
		
		public ArooaSession getArooaSession() {
			return arooaSession;
		}		
	}
	
   @Test
	public void testArchiveAndRestore() throws ArooaPropertyException, 
			ArooaConversionException, SQLException, ComponentPersistException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/persist/FileSilhouetteArchiveTest1.xml",
				getClass().getClassLoader()));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());

		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		SQLPersisterService test = new SQLPersisterService();
		test.setConnection(ct.toValue());
		test.start();
		
		OddjobPersister archiver = test.getPersister(null);
		
		ComponentPersister persister = archiver.persisterFor(null);
		
		ArooaSession session = lookup.lookup("capture.arooaSession", 
				ArooaSession.class);

		Object silhouette  = new SilhouetteFactory().create(
				lookup.lookup("seq"), session);
		
		persister.persist("one", silhouette, session);
		
		oddjob.destroy();
		
		ArooaSession session2 = new OddjobSessionFactory(
				).createSession();
		
		Object[] archives = persister.list();
		assertEquals(1, archives.length);
		assertEquals("one", archives[0]);
		
		Object restored = persister.restore("one", 
				getClass().getClassLoader(), session2);
		
		assertNotNull(restored);
		
		assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(restored));
		
		Object[] children = OddjobTestHelper.getChildren((Structural) restored);
		
		assertEquals(3, children.length);
		
		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(children[0]));
		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(children[1]));
		
		test.stop();
	}	
}
