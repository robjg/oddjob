/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.action;

import junit.framework.TestCase;

import org.oddjob.MockStateful;
import org.oddjob.Stoppable;
import org.oddjob.monitor.model.JobAction;
import org.oddjob.monitor.model.MockExplorerContext;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;
import org.oddjob.util.MockThreadManager;
import org.oddjob.util.ThreadManager;

public class StopActionTest extends TestCase {

	class OurExplorerContext extends MockExplorerContext {
		
		Object component;
		
		@Override
		public Object getThisComponent() {
			return component;
		}
		
		@Override
		public ThreadManager getThreadManager() {
			return new MockThreadManager() {
				@Override
				public void run(Runnable runnable, String description) {
					runnable.run();
				}
			};
		}
	}
	
	/**
	 * Test that performing the action works.
	 *
	 */
	public void testPerform() throws Exception {
		class MyS implements Stoppable {
			boolean stopped = false;
			public void stop() {
				stopped = true;
			}
		}
		MyS sample = new MyS();
				
		OurExplorerContext ec = new OurExplorerContext();
		ec.component = sample;
		
		JobAction test = new StopAction();
		test.setSelectedContext(ec);
		
		test.action();
		
		assertTrue(sample.stopped);
	}

	/**
	 * Test action is enabled when a stateful is selected running.
	 *
	 */
	public void testEnabled() {
		class MySS extends MockStateful implements Stoppable {
			boolean removed;
			public void stop() {}
			public void addJobStateListener(JobStateListener listener) {
				listener.jobStateChange(
						new JobStateEvent(this, JobState.EXECUTING));
			}
			public void removeJobStateListener(JobStateListener listener) {
				removed = true;
			}
		}
		MySS sample = new MySS();
		
		OurExplorerContext ec = new OurExplorerContext();
		ec.component = sample;
		
		JobAction test = new StopAction();

		test.setSelectedContext(ec);
		test.prepare();
		
		assertTrue(test.isEnabled());
		
		test.setSelectedContext(null);
		assertTrue(sample.removed);
	}
	
	/**
	 * Test action is disable when a stateful is selected in complete state.
	 *
	 */
	public void testDisabled() {
		class MySS extends MockStateful implements Stoppable {
			boolean removed;
			public void stop() {}
			public void addJobStateListener(JobStateListener listener) {
				listener.jobStateChange(
						new JobStateEvent(this, JobState.COMPLETE));
			}
			public void removeJobStateListener(JobStateListener listener) {
				removed = true;
			}
		}
		MySS sample = new MySS();
		
		OurExplorerContext ec = new OurExplorerContext();
		ec.component = sample;
		
		JobAction test = new StopAction();

		test.setSelectedContext(ec);
		test.prepare();
		
		assertFalse(test.isEnabled());
		
		test.setSelectedContext(null);
		assertTrue(sample.removed);

	}
	
	/**
	 * Test action is disabled for an object.
	 *
	 */
	public void testWithObject() {
		OurExplorerContext ec = new OurExplorerContext();
		ec.component = new Object();
		
		JobAction test = new StopAction();
		
		test.setSelectedContext(ec);
		test.prepare();
		
		assertFalse(test.isEnabled());
	}

}
