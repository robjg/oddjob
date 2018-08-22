package org.oddjob.scheduling.state;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.state.IsStoppable;
import org.oddjob.state.ParentState;
import org.oddjob.state.ParentStateConverter;
import org.oddjob.state.StandardParentStateConverter;

public class TimerStateTest extends OjTestCase{

   @Test
	public void testHowParentSeesTimerStates() {
		
		ParentStateConverter converter = new StandardParentStateConverter();
		
		assertEquals(ParentState.READY, 
				converter.toStructuralState(TimerState.STARTABLE));
		assertEquals(ParentState.ACTIVE, 
				converter.toStructuralState(TimerState.STARTING));
		assertEquals(ParentState.STARTED, 
				converter.toStructuralState(TimerState.STARTED));
		assertEquals(ParentState.ACTIVE, 
				converter.toStructuralState(TimerState.ACTIVE));
		assertEquals(ParentState.COMPLETE, 
				converter.toStructuralState(TimerState.COMPLETE));
		assertEquals(ParentState.INCOMPLETE, 
				converter.toStructuralState(TimerState.INCOMPLETE));
		assertEquals(ParentState.EXCEPTION, 
				converter.toStructuralState(TimerState.EXCEPTION));
	}
	
   @Test
	public void testSinityChecks() {
		
		assertEquals(false, new IsStoppable().test(TimerState.STARTABLE));
		assertEquals(true, new IsStoppable().test(TimerState.STARTING));
		assertEquals(true, new IsStoppable().test(TimerState.STARTED));
		assertEquals(true, new IsStoppable().test(TimerState.ACTIVE));
	}
}
