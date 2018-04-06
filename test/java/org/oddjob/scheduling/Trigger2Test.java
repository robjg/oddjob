/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.Stateful;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.scheduling.state.TimerState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class Trigger2Test extends OjTestCase {

	private static final Logger logger = 
		LoggerFactory.getLogger(Trigger2Test.class);
	
    @Before
    public void setUp() throws Exception {

		logger.debug("----------------- " + getName() + " -------------");
	}
		
    @Test
 	public void testExpressionExample() throws InterruptedException {
 		

 		Oddjob oddjob = new Oddjob();
 		oddjob.setConfiguration(new XMLConfiguration(
 				"org/oddjob/scheduling/TriggerExpressionExample.xml",
 				getClass().getClassLoader()));
 				
 		oddjob.load();
 		
 		OddjobLookup lookup = new OddjobLookup(oddjob);
 		
 		Stateful test = (Stateful) lookup.lookup("trigger");
 		
 		StateSteps testStates = new StateSteps(test);
 		testStates.startCheck(TimerState.STARTABLE, 
 				TimerState.STARTING, TimerState.STARTED);
 		
 		oddjob.run();
 		
 		assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());

 		testStates.checkNow();
 		testStates.startCheck(TimerState.STARTED, TimerState.ACTIVE, 
 				TimerState.COMPLETE);
 		
 		Runnable thing1 = (Runnable) lookup.lookup("thing1");
 		thing1.run();
 		Runnable thing2 = (Runnable) lookup.lookup("thing2");
 		thing2.run();
 		
 		testStates.checkWait();

 		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
 		
 		oddjob.destroy();
 	}
    
    
}
