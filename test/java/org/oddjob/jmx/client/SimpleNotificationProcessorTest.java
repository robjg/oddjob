/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

public class SimpleNotificationProcessorTest extends TestCase {

	private static final Logger logger = Logger.getLogger(SimpleNotificationProcessorTest.class);
	
	public void testSimpleOneNotification() throws Exception {
		
		final SimpleNotificationProcessor test = 
			new SimpleNotificationProcessor(logger);
		
		class R implements Runnable {
			boolean ran;
			public void run() {
				ran = true; 
				test.stopProcessor();
			}
		}
		R r = new R();
		
		
		test.start();
		
		test.enqueue(r);
		
		test.join();
		
		assertTrue(r.ran);
	}
	
	public void testDelayed() throws Exception {
		final SimpleNotificationProcessor test = 
			new SimpleNotificationProcessor(logger);
		
		class R implements Runnable {
			boolean ran;
			public void run() {
				ran = true; 
				test.stopProcessor();
			}
		}
		R runnable = new R();
		
		
		test.start();
		
		test.enqueueDelayed(runnable, 500);
		
		test.join();
		
		assertTrue(runnable.ran);
	}
}
