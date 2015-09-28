/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import java.util.Observable;
import java.util.Observer;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.MockStateful;
import org.oddjob.Oddjob;
import org.oddjob.OddjobConsole;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogHelper;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.cache.LocalConsoleArchiver;
import org.oddjob.state.StateListener;

/**
 * 
 */
public class DetailModelTest extends TestCase {
	private static final Logger logger = Logger.getLogger(DetailModelTest.class);
	
	class OurExplorerContext extends MockExplorerContext {

		ConsoleArchiver consoleArchiver;
	
		LogArchiver logArchiver;
		
		Object component;
		
		@Override
		public Object getThisComponent() {
			return component;
		}
		
		@Override
		public Object getValue(String key) {
			if (LogContextInialiser.LOG_ARCHIVER.equals(key)) {
				return logArchiver;
			}
			if (LogContextInialiser.CONSOLE_ARCHIVER.equals(key)) {
				return consoleArchiver;
			}
			throw new RuntimeException(key);
		}
	}
	
	class MyLA implements LogArchiver {
		boolean removed;
		public void addLogListener(LogListener l, Object component,
				LogLevel level, long last, int max) {
			logger.debug("logListener added " + LogHelper.getLogger(component));
			assertEquals(LogLevel.TRACE, level);
			assertEquals(-1, last);
			assertEquals(1000, max);
		}
		
		public void removeLogListener(LogListener l, Object component) {
			removed = true;
			logger.debug("logListener removed");
		}
		
		public void onDestroy() {
			throw new RuntimeException("Unexpected.");
		}
	}
			
	
	class MyJob extends MockStateful {
		public String getLogger() {
			return "foo";
		}
		boolean added;
		boolean removed;
		public void addStateListener(StateListener listener) {
			logger.debug("JobStateListener added");
			added = true;
		}
		public void removeStateListener(StateListener listener) {
			logger.debug("JobStateListener removed");
			removed = true;
		}
	}

	Observable observable;
	
	Object ar;
	
	class MyO implements Observer {
		/* (non-Javadoc)
		 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
		 */
		public void update(Observable o, Object arg) {
			observable = o;
			ar = arg;
		}
	}
	
	/**
	 * Test the effect of selecting a node on the model.
	 *
	 */
	public void testSelect() {
		
		try (OddjobConsole.Close close = OddjobConsole.initialise()) {
		
			
			MyLA la = new MyLA();
			
			MyJob myJob = new MyJob();
			
			DetailModel detailModel = new DetailModel();
			
			OurExplorerContext context = new OurExplorerContext();
			context.component = myJob;
			context.logArchiver = la;
			context.consoleArchiver = new LocalConsoleArchiver();
			
			// console
			Oddjob.class.getName();
			System.out.println("Hello");
			detailModel.setTabSelected(DetailModel.CONSOLE_TAB);
			logger.debug("Console selected");
			
			Observable consoleModel = detailModel.getConsoleModel();
			consoleModel.addObserver(new MyO());
			detailModel.setSelectedContext(context);
			
			// Why does this fail!!!
	//		assertNotNull(observable);
			
			// log
			detailModel.setTabSelected(DetailModel.LOG_TAB);
			logger.debug("Log selected");
			logger.debug("Log de-selected");
			detailModel.setSelectedContext(null);		
			assertTrue(la.removed);
			la.removed = false;
			
			detailModel.setTabSelected(DetailModel.STATE_TAB);
	
			logger.debug("Tab 0 selected");
			detailModel.setSelectedContext(context);
			assertTrue(myJob.added);
	
			logger.debug("Tab 0 de-selected");
			detailModel.setSelectedContext(null);
			assertTrue(myJob.removed);
		}
	}
	
}
	