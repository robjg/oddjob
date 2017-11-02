/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.console;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.OddjobConsole;
import org.oddjob.logging.ConsoleOwner;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.cache.LocalConsoleArchiver;
import org.oddjob.logging.cache.LogArchiveImpl;

/**
 * 
 */
public class LocalConsoleArchiverTest extends OjTestCase {

	static final String LS = System.getProperty("line.separator");
	
	class MyLL implements LogListener {
		
		String text;
		long num;
			
		public synchronized void logEvent(LogEvent logEvent) {
			text = logEvent.getMessage();
			num = logEvent.getNumber();
		}
	}

	
	/**
	 * test writing to our global console, and ensuring the listener
	 * picks things up.
	 */
   @Test
	public void testArchiveAndRetrieve() {

		try (OddjobConsole.Close close = OddjobConsole.initialise()) {
			
			// Console archiver will by default be be using Oddjob.CONSOLE
			LocalConsoleArchiver test = new LocalConsoleArchiver();
			
			System.out.println("Some noise");
			
			MyLL ll = new MyLL();
			
			test.addConsoleListener(ll, new Object(), -1, 1000);
	
			synchronized (ll) {
			
				System.out.println("Hello");
					
				long before = ll.num;
			
				assertEquals("Hello" + LS, ll.text);
				
				System.out.println("World");
				
				assertEquals(before + 1, ll.num);
				
				assertEquals("World" + LS, ll.text);
				
				test.removeConsoleListener(ll, new Object());
				
				System.out.println("Won't be listened to");
				
				// should assert as before.
				assertEquals(before + 1, ll.num);
				assertEquals("World" + System.getProperty("line.separator"), ll.text);
			}	
		}
	}

	
	
	/**
	 * test writing to a ConsoleArchive, and ensuring the listener
	 * picks things up.
	 */
   @Test
	public void testConsoleArchive() {
		class CA implements ConsoleOwner {
			LogArchiveImpl la = new LogArchiveImpl("test", 10);
			public LogArchive consoleLog() {
				return la;
			}
		}
		CA ca = new CA();
		
		LocalConsoleArchiver test = new LocalConsoleArchiver();
		

		MyLL ll = new MyLL();
		
		test.addConsoleListener(ll, ca, -1, 1000);

		ca.la.addEvent(LogLevel.INFO, "Hello");
		assertEquals(0, ll.num);
		assertEquals("Hello", ll.text);
		
		ca.la.addEvent(LogLevel.INFO, "World");
		assertEquals(1, ll.num);
		assertEquals("World", ll.text);
		
		test.removeConsoleListener(ll, ca);
		
		ca.la.addEvent(LogLevel.WARN, "Won't be listened to");
		
		// should assert as before.
		assertEquals(1, ll.num);
		assertEquals("World", ll.text);
	}

}
