/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.action;

import junit.framework.TestCase;

import org.oddjob.Resetable;
import org.oddjob.monitor.model.JobAction;
import org.oddjob.monitor.model.MockExplorerContext;
import org.oddjob.util.MockThreadManager;
import org.oddjob.util.ThreadManager;

public class HardResetActionTest extends TestCase {

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
		class MyR implements Resetable {
			boolean reset = false;
			public boolean softReset() {
				throw new RuntimeException("Unexpected.");
			}
			public boolean hardReset() {
				reset = true;
				return true;
			}
		}
		MyR sample = new MyR();
				
		OurExplorerContext ec = new OurExplorerContext();
		ec.component = sample;
		
		JobAction test = new HardResetAction();
		test.setSelectedContext(ec);
		
		test.action();
		
		assertTrue(sample.reset);
	}

	/**
	 * Test action is disabled for an object.
	 *
	 */
	public void testWithObject() {
		OurExplorerContext eContext = new OurExplorerContext();
		eContext.component = new Object();
		
		JobAction test = new HardResetAction();
		
		test.setSelectedContext(eContext);
		test.prepare();
		
		assertFalse(test.isEnabled());
	}

}
