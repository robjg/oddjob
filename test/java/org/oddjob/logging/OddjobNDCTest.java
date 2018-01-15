/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.logging;

import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.framework.ComponentBoundry;
import org.oddjob.logging.appender.AppenderArchiver;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OddjobNDCTest extends OjTestCase {
	
	private static final Logger logger = LoggerFactory.getLogger(OddjobNDCTest.class);

    @Test
	public void testContextTracksPushesAsExpected() {
		
		String loggerName1 = "org.oddjob.TestLogger1";
		String loggerName2 = "org.oddjob.TestLogger2";

		Object job1 = new Object();
		Object job2 = new Object();
				
		try (Restore r1 = ComponentBoundry.push(loggerName1, job1)) {
			assertEquals(loggerName1, OddjobNDC.current().get().getLogger());
			assertEquals(job1, OddjobNDC.current().get().getJob());
			
			try (Restore r2 = ComponentBoundry.push(loggerName2, job2)) {
				assertEquals(loggerName2, OddjobNDC.current().get().getLogger());
				assertEquals(job2, OddjobNDC.current().get().getJob());
			}
			
			assertEquals(loggerName1, OddjobNDC.current().get().getLogger());
			assertEquals(job1, OddjobNDC.current().get().getJob());
		}
				
		assertThat(OddjobNDC.current().isPresent(), is(false));
	}

    @Test
	public void testEmptyPeek() {
		
		assertThat(OddjobNDC.current().isPresent(), is(false));
	}
	
	
	private class MyLL implements LogListener {
		final List<String> messages = new ArrayList<>();
		
		public void logEvent(LogEvent logEvent) {
			messages.add(logEvent.getMessage());
		}
	}
	
	static private class OurLogEnabled implements LogEnabled {
	
		public String loggerName() {
			return "our.unique.logger";
		}
	}
		
    @Test
	public void givenArchiverWhenLoggingThenOnlyCorrectContextGetsArchived() {
		
 	   OurLogEnabled logEnabled = new OurLogEnabled();

	   AppenderArchiver archiver = new AppenderArchiver(logEnabled, "%m");
		
		MyLL ll = new MyLL();
		archiver.addLogListener(ll, logEnabled, LogLevel.INFO, -1, 100);
		
		logger.info("Will not be archived!");
		
		assertThat(ll.messages.isEmpty(), is(true));
		
		try (Restore r1 = OddjobNDC.push(logEnabled.loggerName(), new Object())) {

			logger.info("Will be archived!");
	
			try (Restore r2 = OddjobNDC.push("SomethingElse", new Object())) {
				
				logger.info("Also Will not be archived!");
			}	
		}
		
		assertThat(ll.messages.size(), is(1));				
		assertThat(ll.messages.get(0), is("Will be archived!"));
		
	}

    @Test
	public void testChildThread() throws InterruptedException {
		
 	    OurLogEnabled logEnabled = new OurLogEnabled();

   	    String job = "My Important Job";
		
		AppenderArchiver archiver = new AppenderArchiver(logEnabled, "[%X{ojname}] %m");
		
		MyLL ll = new MyLL();
		archiver.addLogListener(ll, logEnabled, LogLevel.INFO, -1, 100);
		
		try (Restore r = OddjobNDC.push(logEnabled.loggerName(), job)) {

			Thread t = new Thread(new Runnable() {
				
				public void run() {
					logger.info("Child Thread Message.");
				}
			});
		
			t.start();	
			t.join();
		}		
		
		// Note that MDC is no longer set automatically in thread in SLF4J.
		assertEquals("[] Child Thread Message.", ll.messages.get(0));
		
	}

}
