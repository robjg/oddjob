/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.state;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.MockStateful;
import org.oddjob.Structural;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * 
 */
public class StructuralStateHelperTest extends OjTestCase {
	
	State state;

	class OurStateListener implements StateListener {
		public void jobStateChange(StateEvent event) {
			state = event.getState();
		}		
	}
	
	class DummyStructural implements Structural {
		public void addStructuralListener(StructuralListener listener) {
			throw new RuntimeException("Unexpected.");
		}
		public void removeStructuralListener(StructuralListener listener) {
			throw new RuntimeException("Unexpected.");
		}
	}
	
	// test with lots of children
   @Test
	public void testManyDifferentChildren() {
		FlagState j1 = new FlagState(JobState.COMPLETE);
		
		Object j2 = new Object();
		
		Object j3 = new FlagState(JobState.COMPLETE);
		
		Object j4 = new FlagState(JobState.EXCEPTION);
		
		FlagState j5 = new FlagState(JobState.INCOMPLETE);
		
		ChildHelper<Object> childHelper = new ChildHelper<Object>(new DummyStructural());
	
		StructuralStateHelper test = new StructuralStateHelper(
				childHelper, new AnyActiveStateOp());
		
		test.addStateListener(new OurStateListener());
		
		assertEquals(ParentState.READY, state);
		
		childHelper.insertChild(0, j1);
		childHelper.insertChild(1, j2);
		childHelper.insertChild(2, j3);
		childHelper.insertChild(3, j4);
		childHelper.insertChild(4, j5);
		
		assertEquals(ParentState.READY, state);
		
		j1.run();

		assertEquals(ParentState.READY, state);
		
		// j3 wrapper
		((Runnable) childHelper.getChildAt(2)).run();

		assertEquals(ParentState.READY, state);
		
		// j4 wrapper
		((Runnable) childHelper.getChildAt(3)).run();
		
		assertEquals(ParentState.EXCEPTION, state);
		
		childHelper.removeChildAt(3);
		
		j5.run();

		assertEquals(ParentState.INCOMPLETE, state);
		
		childHelper.removeChildAt(3);

		j1.hardReset();
		j1.run();
		
		assertEquals(ParentState.COMPLETE, state);
		
		childHelper.removeChildAt(2);
		childHelper.insertChild(2, j5);
		
		j5.hardReset();
		j5.run();
		
		assertEquals(ParentState.INCOMPLETE, state);
		
		childHelper.softResetChildren();
		
		assertEquals(ParentState.READY, state);
	}

	// If a job just contains a folder like object
	// it should be complete.
   @Test
	public void testLikeFolder() {
		Object j1 = new Object();
		
		ChildHelper<Object> childHelper = new ChildHelper<Object>(new DummyStructural());
		
		StructuralStateHelper test = new StructuralStateHelper(
				childHelper, new AnyActiveStateOp());
		
		test.addStateListener(new OurStateListener());
		childHelper.insertChild(0, j1);
		
		assertEquals(ParentState.COMPLETE, state);
	}

	
	// one runnable one not
   @Test
	public void testTwo() {
		Object j1 = new Object();
		Object j2 = new FlagState(JobState.COMPLETE);
		
		ChildHelper<Object> childHelper = new ChildHelper<Object>(new DummyStructural());
		
		StructuralStateHelper test = new StructuralStateHelper(
				childHelper, new AnyActiveStateOp());
		
		test.addStateListener(new OurStateListener());

		childHelper.insertChild(0, j1);
		childHelper.insertChild(1, j2);		
		
		assertEquals(ParentState.READY, state);
		
		((Runnable) childHelper.getChildAt(1)).run();

		assertEquals(ParentState.COMPLETE, state);
	}
	
	
   @Test
	public void testEmpty() {
		ChildHelper<Object> childHelper = new ChildHelper<Object>(new DummyStructural());
	
		StructuralStateHelper test = new StructuralStateHelper(
				childHelper, states -> null);
		
		test.addStateListener(new OurStateListener());
		
		assertEquals(ParentState.READY, state);
	}
	
	private class OurStateful extends MockStateful {
		
		StateListener listener;
		
		public void addStateListener(StateListener listener) {
			assertNull(this.listener);
			this.listener = listener;
			listener.jobStateChange(new StateEvent(this, JobState.READY));
		}
		
		public void removeStateListener(StateListener listener) {
			assertEquals(this.listener, listener);
			this.listener = null;
		}
		
		
	}
	
	/**
	 * StructuralStateHelper can't cope with DESTROYED. children
	 * must be removed before they are destroyed - which is what 
	 * Arooa does.
	 */
   @Test
	public void testDestroyed() {
		
		ChildHelper<Object> childHelper = new ChildHelper<Object>(new DummyStructural());
		
		StructuralStateHelper test = new StructuralStateHelper(
				childHelper, new AnyActiveStateOp());
		
		OurStateful job = new OurStateful();

		childHelper.insertChild(0, new FlagState(JobState.COMPLETE));
		childHelper.insertChild(1, job);
		
		assertNotNull(job.listener);

		try {
			job.listener.jobStateChange(new StateEvent(
				job, JobState.DESTROYED));
			fail("Should Fail.");
		} catch (Exception e) {
			// Expected.
		}
		
		childHelper.removeChildAt(1);
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
	}
}
