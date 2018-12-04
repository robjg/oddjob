package org.oddjob.state;

import org.junit.Test;


import org.oddjob.OjTestCase;

public class CompleteOrNotOpTest extends OjTestCase {

	static class JobEvents {

		static StateEvent READY = ConstStateful.event(JobState.READY);

		static StateEvent EXECUTING = ConstStateful.event(JobState.EXECUTING);

		static StateEvent EXCEPTION = ConstStateful.exception(JobState.EXCEPTION, new RuntimeException());

		static StateEvent INCOMPLETE = ConstStateful.event(JobState.INCOMPLETE);

		static StateEvent COMPLETE = ConstStateful.event(JobState.COMPLETE);

		static StateEvent DESTROYED = ConstStateful.event(JobState.DESTROYED);
	}

	@Test
	public void testVariousStates() {
		
		CompleteOrNotOp test = new CompleteOrNotOp();
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate().getState());
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobEvents.COMPLETE).getState());
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobEvents.COMPLETE, JobEvents.COMPLETE, JobEvents.COMPLETE).getState());
		
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobEvents.COMPLETE, JobEvents.INCOMPLETE, JobEvents.COMPLETE).getState());
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobEvents.READY).getState());

		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobEvents.EXECUTING).getState());
		
		try {
			test.evaluate(JobEvents.DESTROYED);
			fail("Should fail.");
		} catch (IllegalStateException e) {
			// expected.
		}
	}
	
}
