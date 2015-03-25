package org.oddjob.sql;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.scheduling.state.TimerState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;

public class SQLSilhouettesWithArchiveTest extends TestCase {

	private static final Logger logger = 
		Logger.getLogger(SQLSilhouettesWithArchiveTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.debug("-----------------  " + getName() + "  -----------------");
	}
	
	public void testSimple() throws ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SQLSilhouettesWithArchiveTest.xml",
				getClass().getClassLoader()));
		
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Stateful timer1 = lookup.lookup("timer1", Stateful.class);
		Stateful timer2 = lookup.lookup("timer2", Stateful.class);
		
		StateSteps timer1States = new StateSteps(timer1);
		timer1States.setTimeout(20*1000L);
		StateSteps timer2States = new StateSteps(timer2);
		timer2States.setTimeout(20*1000L);
		
		timer1States.startCheck(TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.STARTED, TimerState.COMPLETE);
		timer2States.startCheck(TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.STARTED, TimerState.COMPLETE);
		
		oddjob.run();
		
		assertEquals(ParentState.ACTIVE, oddjob.lastStateEvent().getState());
		
		timer1States.checkWait();
		timer2States.checkWait();
		
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
