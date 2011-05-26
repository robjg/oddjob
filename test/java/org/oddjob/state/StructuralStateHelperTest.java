/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.state;

import junit.framework.TestCase;

import org.oddjob.MockStateful;
import org.oddjob.Structural;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * 
 */
public class StructuralStateHelperTest extends TestCase {
	
	JobState state;

	class OurStateListener implements JobStateListener {
		public void jobStateChange(JobStateEvent event) {
			state = event.getJobState();
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
	public void testManyDifferentChildren() {
		FlagState j1 = new FlagState(JobState.COMPLETE);
		
		Object j2 = new Object();
		
		Object j3 = new FlagState(JobState.COMPLETE);
		
		Object j4 = new FlagState(JobState.EXCEPTION);
		
		FlagState j5 = new FlagState(JobState.INCOMPLETE);
		
		ChildHelper<Object> childHelper = new ChildHelper<Object>(new DummyStructural());
	
		StructuralStateHelper test = new StructuralStateHelper(
				childHelper, new WorstStateOp());
		
		test.addJobStateListener(new OurStateListener());
		
		assertEquals(JobState.READY, state);
		
		childHelper.insertChild(0, j1);
		childHelper.insertChild(1, j2);
		childHelper.insertChild(2, j3);
		childHelper.insertChild(3, j4);
		childHelper.insertChild(4, j5);
		
		assertEquals(JobState.READY, state);
		
		j1.run();

		assertEquals(JobState.READY, state);
		
		// j3 wrapper
		((Runnable) childHelper.getChildAt(2)).run();

		assertEquals(JobState.READY, state);
		
		// j4 wrapper
		((Runnable) childHelper.getChildAt(3)).run();
		
		assertEquals(JobState.EXCEPTION, state);
		
		childHelper.removeChildAt(3);
		
		j5.run();

		assertEquals(JobState.INCOMPLETE, state);
		
		childHelper.removeChildAt(3);

		j1.hardReset();
		j1.run();
		
		assertEquals(JobState.COMPLETE, state);
		
		childHelper.removeChildAt(2);
		childHelper.insertChild(2, j5);
		
		j5.hardReset();
		j5.run();
		
		assertEquals(JobState.INCOMPLETE, state);
		
		childHelper.softResetChildren();
		
		assertEquals(JobState.READY, state);
	}

	// If a job just contains a folder like object
	// it should be complete.
	public void testLikeFolder() {
		Object j1 = new Object();
		
		ChildHelper<Object> childHelper = new ChildHelper<Object>(new DummyStructural());
		
		StructuralStateHelper test = new StructuralStateHelper(
				childHelper, new WorstStateOp());
		
		test.addJobStateListener(new OurStateListener());
		childHelper.insertChild(0, j1);
		
		assertEquals(JobState.COMPLETE, state);
	}

	
	// one runnable one not
	public void testTwo() {
		Object j1 = new Object();
		Object j2 = new FlagState(JobState.COMPLETE);
		
		ChildHelper<Object> childHelper = new ChildHelper<Object>(new DummyStructural());
		
		StructuralStateHelper test = new StructuralStateHelper(
				childHelper, new WorstStateOp());
		
		test.addJobStateListener(new OurStateListener());

		childHelper.insertChild(0, j1);
		childHelper.insertChild(1, j2);		
		
		assertEquals(JobState.READY, state);
		
		((Runnable) childHelper.getChildAt(1)).run();

		assertEquals(JobState.COMPLETE, state);
	}
	
	
	public void testEmpty() {
		ChildHelper<Object> childHelper = new ChildHelper<Object>(new DummyStructural());
	
		StructuralStateHelper h = new StructuralStateHelper(
				childHelper, new WorstStateOp());
		
		h.addJobStateListener(new OurStateListener());
		
		assertEquals(JobState.READY, state);
	}
	
	private class OurStateful extends MockStateful {
		
		JobStateListener listener;
		
		public void addJobStateListener(JobStateListener listener) {
			assertNull(this.listener);
			this.listener = listener;
		}
		
		public void removeJobStateListener(JobStateListener listener) {
			assertEquals(this.listener, listener);
			this.listener = null;
		}
		
		
	}
	
	/**
	 * StructuralStateHelper can't cope with DESTROYED. children
	 * must be removed before they are destroyed - which is what 
	 * Arooa does.
	 */
	public void testDestroyed() {
		
		ChildHelper<Object> childHelper = new ChildHelper<Object>(new DummyStructural());
		
		StructuralStateHelper test = new StructuralStateHelper(
				childHelper, new WorstStateOp());
		
		OurStateful job = new OurStateful();

		childHelper.insertChild(0, new FlagState(JobState.COMPLETE));
		childHelper.insertChild(1, job);
		
		assertNotNull(job.listener);

		try {
			job.listener.jobStateChange(new JobStateEvent(
				job, JobState.DESTROYED));
			fail("Should Fail.");
		} catch (Exception e) {
			// Expected.
		}
		
		childHelper.removeChildAt(1);
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
	}
}
