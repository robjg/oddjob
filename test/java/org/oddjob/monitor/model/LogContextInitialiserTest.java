package org.oddjob.monitor.model;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.MockLogArchiver;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.util.ThreadManager;

public class LogContextInitialiserTest extends TestCase {

	class OurModel extends MockExplorerModel {
		
		LogArchiver logArchiver = new MockLogArchiver();
		
		@Override
		public Oddjob getOddjob() {
			return new Oddjob();
		}
		
		@Override
		public ConsoleArchiver getConsoleArchiver() {
			return null;
		}
		
		@Override
		public ThreadManager getThreadManager() {
			return null;
		}
		
		@Override
		public LogArchiver getLogArchiver() {
			return logArchiver;
		}
		
		@Override
		public ContextInitialiser[] getContextInitialisers() {
			return new ContextInitialiser[] {
					new LogContextInialiser(this)
			};
		}
	}
	
	
	public void testNextLevelLogArchiver() {
		
		OurModel model = new OurModel();
		
		ExplorerContext context = new ExplorerContextImpl(model);
		
		ExplorerContext context2 = context.addChild(new Object());
				
		assertEquals(model.logArchiver, context2.getValue(
				LogContextInialiser.LOG_ARCHIVER));
	}
	
	public void testNextLevelIsALogArchiver() {
		
		OurModel model = new OurModel();
		
		ExplorerContext context = new ExplorerContextImpl(model);
		
		ExplorerContext context2 = context.addChild(new MockLogArchiver());
				
		assertEquals(model.logArchiver, context2.getValue(
				LogContextInialiser.LOG_ARCHIVER));
	}
}
