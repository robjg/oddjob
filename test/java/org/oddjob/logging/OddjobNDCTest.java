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

		Object job1 = new Object();
		Object job2 = new Object();
				
		OddjobNDC.push(loggerName1, job1);
		assertEquals(loggerName1, OddjobNDC.peek().getLogger());
		assertEquals(job1, OddjobNDC.peek().getJob());
		
		OddjobNDC.push(loggerName2, job2);
		assertEquals(loggerName2, OddjobNDC.peek().getLogger());
		assertEquals(job2, OddjobNDC.peek().getJob());
		
		assertEquals(loggerName2, OddjobNDC.pop().getLogger());
		assertEquals(loggerName1, OddjobNDC.pop().getLogger());		
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
		
		OddjobNDC.push(loggerName(), new Object());

		logger.info("Will be archived!");
		assertEquals("Will be archived!" + Helper.LS, ll.message);
				
		OddjobNDC.pop();
	}

	public void testChildThread() throws InterruptedException {
		
		String job = "My Important Job";
		
		Log4jArchiver archiver = new Log4jArchiver(this, "[%X{ojname}] %m%n");
		
		MyLL ll = new MyLL();
		archiver.addLogListener(ll, this, LogLevel.INFO, -1, 100);
		
		OddjobNDC.push(loggerName(), job);

		Thread t = new Thread(new Runnable() {
			public void run() {
				logger.info("Child Thread Message.");
				
			}
		});

		
		OddjobNDC.pop();
		
		t.start();
		
		t.join();
		
		assertEquals("[My Important Job] Child Thread Message." + Helper.LS, ll.message);
		

	}

}
