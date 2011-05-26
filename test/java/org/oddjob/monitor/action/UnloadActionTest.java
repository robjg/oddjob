package org.oddjob.monitor.action;

import junit.framework.TestCase;

import org.oddjob.Loadable;
import org.oddjob.monitor.model.MockExplorerContext;
import org.oddjob.util.MockThreadManager;
import org.oddjob.util.ThreadManager;

public class UnloadActionTest extends TestCase {

	private class OurLoadable implements Loadable {
		
		boolean loadable = false;
		
		public boolean isLoadable() {
			return loadable;
		}
		
		public void load() {
			throw new RuntimeException("Unexpected.");
		}
		
		@Override
		public void unload() {
			setLoadable(true);
		}
		
		void setLoadable(boolean loadable) {
			this.loadable = loadable;
		}
	}
	
	class OurEContext extends MockExplorerContext {
		
		OurLoadable loadable = new OurLoadable();
		
		@Override
		public Object getThisComponent() {
			return loadable;
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
	
	public void testCycle() throws Exception {
		
		UnloadAction test = new UnloadAction();
		assertTrue(test.isEnabled());
		assertTrue(test.isVisible());
		
		OurEContext eContext = new OurEContext();
		
		test.setSelectedContext(eContext);
		
		assertTrue(test.isEnabled());
		assertTrue(test.isVisible());
		
		assertFalse(eContext.loadable.loadable);
		
		test.action();
		
		test.setSelectedContext(eContext);
		test.prepare();
		
		assertTrue(eContext.loadable.loadable);
		
		assertFalse(test.isEnabled());
		assertTrue(test.isVisible());
		
		test.setSelectedContext(null);
	}
	
}
