/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.logging;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Helper;
import org.oddjob.logging.log4j.Log4jArchiver;

public class OddjobNDCTest extends TestCase implements LogEnabled {
	
	private static final Logger logger = Logger.getLogger(OddjobNDCTest.class);

	public void testAll() {
		
		String loggerName1 = "org.oddjob.TestLogger1";
		String loggerName2 = "org.oddjob.TestLogger2";
		
		OddjobNDC.push(loggerName1);
		assertEquals(loggerName1, OddjobNDC.peek());
		OddjobNDC.push(loggerName2);
		assertEquals(loggerName2, OddjobNDC.peek());
		
		assertEquals(loggerName2, OddjobNDC.pop());
		assertEquals(loggerName1, OddjobNDC.pop());		
	}

	public void testEmptyPeek() {
		
		assertEquals(null, OddjobNDC.peek());
	}
	
	
	class MyLL implements LogListener {
		String message;
		public void logEvent(LogEvent logEvent) {
			message = logEvent.getMessage();
		}
	}
	
	public String loggerName() {
		return "our.unique.logger";
	}
	
	public void testWithArchiver() {
		
		Log4jArchiver archiver = new Log4jArchiver(this, "%m%n");
		
		MyLL ll = new MyLL();
		archiver.addLogListener(ll, this, LogLevel.INFO, -1, 100);
		
		logger.info("Will not be archived!");
		
		assertNull(ll.message);
		
		OddjobNDC.push(loggerName());

		logger.info("Will be archived!");
		assertEquals("Will be archived!" + Helper.LS, ll.message);
				
		OddjobNDC.pop();
	}

	public void testChildThread() throws InterruptedException {
		
		Log4jArchiver archiver = new Log4jArchiver(this, "%m%n");
		
		MyLL ll = new MyLL();
		archiver.addLogListener(ll, this, LogLevel.INFO, -1, 100);
		
		OddjobNDC.push(loggerName());

		Thread t = new Thread(new Runnable() {
			public void run() {
				logger.info("Child Thread Message.");
				
			}
		});

		
		OddjobNDC.pop();
		
		t.start();
		
		t.join();
		
		assertEquals("Child Thread Message." + Helper.LS, ll.message);
		

	}

}
