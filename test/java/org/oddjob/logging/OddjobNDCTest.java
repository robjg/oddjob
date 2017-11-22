/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.logging;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.framework.ComponentBoundry;
import org.oddjob.logging.appender.AppenderArchiver;
import org.oddjob.tools.OddjobTestHelper;

public class OddjobNDCTest extends OjTestCase implements LogEnabled {
	
	private static final Logger logger = Logger.getLogger(OddjobNDCTest.class);

   @Test
	public void testAll() {
		
		String loggerName1 = "org.oddjob.TestLogger1";
		String loggerName2 = "org.oddjob.TestLogger2";

		Object job1 = new Object();
		Object job2 = new Object();
				
		ComponentBoundry.push(loggerName1, job1);
		assertEquals(loggerName1, OddjobNDC.peek().getLogger());
		assertEquals(job1, OddjobNDC.peek().getJob());
		
		ComponentBoundry.push(loggerName2, job2);
		assertEquals(loggerName2, OddjobNDC.peek().getLogger());
		assertEquals(job2, OddjobNDC.peek().getJob());
		
		assertEquals(loggerName2, OddjobNDC.pop().getLogger());
		assertEquals(loggerName1, OddjobNDC.pop().getLogger());		
	}

   @Test
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
	
   @Test
	public void testWithArchiver() {
		
		AppenderArchiver archiver = new AppenderArchiver(this, "%m%n");
		
		MyLL ll = new MyLL();
		archiver.addLogListener(ll, this, LogLevel.INFO, -1, 100);
		
		logger.info("Will not be archived!");
		
		assertNull(ll.message);
		
		OddjobNDC.push(loggerName(), new Object());

		logger.info("Will be archived!");
		assertEquals("Will be archived!" + OddjobTestHelper.LS, ll.message);
				
		OddjobNDC.pop();
	}

   @Test
	public void testChildThread() throws InterruptedException {
		
		String job = "My Important Job";
		
		AppenderArchiver archiver = new AppenderArchiver(this, "[%X{ojname}] %m%n");
		
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
		
		assertEquals("[My Important Job] Child Thread Message." + OddjobTestHelper.LS, ll.message);
		

	}

}
