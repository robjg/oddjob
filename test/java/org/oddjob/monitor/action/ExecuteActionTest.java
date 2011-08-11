/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.action;

import junit.framework.TestCase;

import org.oddjob.MockStateful;
import org.oddjob.monitor.model.JobAction;
import org.oddjob.monitor.model.MockExplorerContext;
import org.oddjob.state.JobState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.util.MockThreadManager;
import org.oddjob.util.ThreadManager;

public class ExecuteActionTest extends TestCase {

	class OurThreadManager extends MockThreadManager {
		
		public void run(Runnable runnable, String description) {
			runnable.run();
		}		
	}
	
	class OurExplorerContext extends MockExplorerContext {

		final OurThreadManager threadManager = new OurThreadManager();
		
		Object component;
		
		@Override
		public Object getThisComponent() {
			return component;
		}
		
		public ThreadManager getThreadManager() {
			return threadManager;
		}
	}
	
	/**
	 * Test that performing the action works.
	 *
	 */
	public void testPerform() throws Exception {
		class MyR implements Runnable {
			boolean ran = false;
			public void run() {
				ran = true;
			}
		}
		
		MyR sample = new MyR();

		OurExplorerContext ec = new OurExplorerContext();
		ec.component = sample;
		
		JobAction test = new ExecuteAction();
		test.setSelectedContext(ec);
		
		test.action();
		
		assertTrue(sample.ran);
	}

	/**
	 * Test action is enabled when a stateful is selected in ready state.
	 *
	 */
	public void testEnabled() {
		class MySR extends MockStateful implements Runnable {
			boolean removed;
			public void run() {}
			public void addStateListener(StateListener listener) {
				listener.jobStateChange(
						new StateEvent(this, JobState.READY));
			}
			public void removeStateListener(StateListener listener) {
				removed = true;
			}
		}
		MySR sample = new MySR();
		
		OurExplorerContext ec = new OurExplorerContext();
		ec.component = sample;
		
		JobAction test = new ExecuteAction();

		test.setSelectedContext(ec);
		test.prepare();
		
		assertTrue(test.isEnabled());
		
		test.setSelectedContext(null);
		assertTrue(sample.removed);
	}
	
	/**
	 * Test action is disable when a stateful is selected in ready complete.
	 *
	 */
	public void testDisabled() {
		class MySR extends MockStateful implements Runnable {
			boolean removed;
			public void run() {}
			public void addStateListener(StateListener listener) {
				listener.jobStateChange(
						new StateEvent(this, JobState.COMPLETE));
			}
			public void removeStateListener(StateListener listener) {
				removed = true;
			}
		}
		MySR sample = new MySR();
		
		OurExplorerContext ec = new OurExplorerContext();
		ec.component = sample;
		
		JobAction test = new ExecuteAction();

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
		OurExplorerContext explorerContext = new OurExplorerContext();
		explorerContext.component = new Object();
		
		JobAction test = new ExecuteAction();
		test.setSelectedContext(explorerContext);
		test.prepare();
		
		assertFalse(test.isEnabled());
	}
}
