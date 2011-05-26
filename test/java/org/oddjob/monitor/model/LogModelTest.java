/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import java.util.Observable;
import java.util.Observer;

import junit.framework.TestCase;

import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;

/**
 * 
 */
public class LogModelTest extends TestCase implements LogEventProcessor {

	String message;
	
	public void testMessage() {
		class MyOb implements Observer {
			public void update(Observable o, Object arg) {
				((LogAction) arg).accept(LogModelTest.this);
			}
		}
		MyOb ob = new MyOb();
		
		LogModel test = new LogModel();
		test.addObserver(ob);
		test.logEvent(new LogEvent("foo", 2, LogLevel.DEBUG, "Hello"));
		assertEquals("Hello", message);
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.monitor.model.LogEventProcessor#onClear()
	 */
	public void onClear() {
		// TODO Auto-generated method stub

	}
	/* (non-Javadoc)
	 * @see org.oddjob.monitor.model.LogEventProcessor#onEvent(java.lang.String, org.oddjob.logging.LogLevel)
	 */
	public void onEvent(String text, LogLevel level) {
		message = text;
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.monitor.model.LogEventProcessor#onUnavailable()
	 */
	public void onUnavailable() {
		// TODO Auto-generated method stub

	}
}

