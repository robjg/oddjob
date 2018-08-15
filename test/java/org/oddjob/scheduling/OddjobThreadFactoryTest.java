package org.oddjob.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.oddjob.arooa.logging.Appender;
import org.oddjob.arooa.logging.AppenderAdapter;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.logging.LoggingEvent;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OddjobThreadFactoryTest {

	private static final Logger logger = LoggerFactory.getLogger(OddjobThreadFactoryTest.class);
		
	@Test
	public void testLogContextInherited() throws InterruptedException {
		

		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				logger.info("Hello from thread " + Thread.currentThread().getName());
			}
		};
		
		List<String> results = new ArrayList<>();
		
		Appender appender = new Appender() {
			@Override
			public void append(LoggingEvent event) {
				results.add(event.getMdc(OddjobNDC.MDC_JOB_NAME));
			}
		};
		
		AppenderAdapter appenderAdapter = LoggerAdapter.appenderAdapterFor(logger.getName());
		appenderAdapter.addAppender(appender);
		
		try (Restore restore = ComponentBoundary.push("our.logger", "Some-Job")) {
						
			ThreadFactory test = new OddjobThreadFactory("Our-Thread");

			Thread t = test.newThread(r);
			t.start();
			
			t.join();
			
			Assert.assertThat(results.get(0), CoreMatchers.is("Some-Job"));
		}
		
		
	}
}
