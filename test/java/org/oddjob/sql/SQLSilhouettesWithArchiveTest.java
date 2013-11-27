package org.oddjob.sql;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.OddjobTestHelper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.WaitJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateConditions;

public class SQLSilhouettesWithArchiveTest extends TestCase {

	private static final Logger logger = 
		Logger.getLogger(SQLSilhouettesWithArchiveTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.debug("-----------------  " + getName() + "  -----------------");
	}
	
	public void testSimple() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SQLSilhouettesWithArchiveTest.xml",
				getClass().getClassLoader()));
		
		oddjob.run();
		
		assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Object timer1 = lookup.lookup("timer1");
		
		WaitJob wait1 = new WaitJob();
		wait1.setFor(timer1);
		wait1.setState(StateConditions.COMPLETE);
		
		wait1.run();
		
		Object timer2 = lookup.lookup("timer2");
		
		WaitJob wait2 = new WaitJob();
		wait2.setFor(timer2);
		wait2.setState(StateConditions.COMPLETE);
		
		wait2.run();
		
		/////////
		
		Object browser1 = lookup.lookup("browser1");

		((Runnable) browser1).run();
		
		Object[] archives1 = OddjobTestHelper.getChildren(browser1);
		
		assertEquals(5, archives1.length);
		
		((Runnable) archives1[4]).run();
		
		Object[] silhouettes1 = OddjobTestHelper.getChildren(archives1[4]);
		
		assertEquals(1, silhouettes1.length);

		assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(silhouettes1[0]));
		
		/////////
		
		Object browser2 = lookup.lookup("browser2");

		((Runnable) browser2).run();
		
		Object[] archives2 = OddjobTestHelper.getChildren(browser2);
		
		assertEquals(5, archives2.length);
		
		((Runnable) archives2[4]).run();
		
		Object[] silhouettes2 = OddjobTestHelper.getChildren(archives2[4]);
		
		assertEquals(1, silhouettes2.length);

		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(silhouettes2[0]));
		
		oddjob.destroy();		
	}
	
}
