/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.action;

import junit.framework.TestCase;

import org.oddjob.Stoppable;
import org.oddjob.monitor.model.JobAction;
import org.oddjob.monitor.model.MockExplorerContext;
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
		
		assertTrue(test.isEnabled());
		
		test.action();
		
		assertTrue(sample.stopped);
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
