package org.oddjob.jmx.handlers;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.LogPollable;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.*;
import org.oddjob.logging.*;

public class LogPollableHandlerFactoryTest extends OjTestCase {

	class OurServerSideToolkit extends MockServerSideToolkit {
		
		@Override
		public ServerContext getContext() {
			return new MockServerContext() {
				@Override
				public ServerId getServerId() {
					return new ServerId("//test");
				}
				@Override
				public ConsoleArchiver getConsoleArchiver() {
					return new MockConsoleArchiver() {
						@Override
						public String consoleIdFor(Object component) {
							return "abc";
						}
					};
				}
			};
		}
	}

	class OurClientToolkit extends MockClientSideToolkit {

		ServerInterfaceHandler handler;

		@SuppressWarnings("unchecked")
		@Override
		public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
				throws Throwable {
			return (T) handler.invoke(
					remoteOperation,
					args);
		}
	}
		
	
   @Test
	public void testIds() {
		
		Object component = new Object();
	
		ServerInterfaceHandlerFactory<Object, LogPollable> test = 
			new LogPollableHandlerFactory();
		
		ServerInterfaceHandler serverHandler = test.createServerHandler(
				component, new OurServerSideToolkit());
		
		OurClientToolkit toolkit = new OurClientToolkit();
		toolkit.handler = serverHandler;
				
		LogPollable client = (LogPollable) 
			new LogPollableHandlerFactory.ClientFactory(
					).createClientHandler(null, toolkit); 
		
		String consoleId = client.consoleId();
		
		assertEquals("abc", consoleId);
	}
	
	class SecondServerSideToolkit extends MockServerSideToolkit {
		
		LogArchiver archiver;
		
		@Override
		public ServerContext getContext() {
			return new MockServerContext() {
				@Override
				public LogArchiver getLogArchiver() {
					return archiver;
				}
				@Override
				public ConsoleArchiver getConsoleArchiver() {
					return new MockConsoleArchiver() {
						@Override
						public String consoleIdFor(Object component) {
							return "abc";
						}
					};
				}
				@Override
				public ServerId getServerId() {
					return new ServerId("//test");
				}
			};
		}
	}
	
	class OurArchiver implements LogArchiver {
		
		LogListener l;
		
		LogEvent[] logEvents = {
			new LogEvent("thing", 24L, LogLevel.DEBUG, "1"),
			new LogEvent("thing", 25L, LogLevel.DEBUG, "2"),
			new LogEvent("thing", 26L, LogLevel.DEBUG, "3"),
			new LogEvent("thing", 27L, LogLevel.DEBUG, "4"),
			new LogEvent("thing", 28L, LogLevel.DEBUG, "5")
		};
		
		public void addLogListener(LogListener l, Object component,
				LogLevel level, long last, int max) {
			this.l = l;
			
			for (long seq = last; seq < 5 && seq < last + max; ++seq) {
				l.logEvent(logEvents[(int) seq]);
			}
		}
		public void removeLogListener(LogListener l, Object component) {
			this.l = null;
		}
		
		public void onDestroy() {
			throw new RuntimeException("Unexpected");
		}
	}
	
   @Test
	public void testRetrieveLogEvents() {
		
		OurArchiver archiver = new OurArchiver();
		
		ServerInterfaceHandlerFactory<Object, LogPollable> test = 
			new LogPollableHandlerFactory();
		
		SecondServerSideToolkit serverKit = new SecondServerSideToolkit();
		serverKit.archiver = archiver;
		
		ServerInterfaceHandler serverHandler = test.createServerHandler(
				null, serverKit);
		
		OurClientToolkit toolkit = new OurClientToolkit();
		toolkit.handler = serverHandler;
				
		LogPollable client = (LogPollable) 
			new LogPollableHandlerFactory.ClientFactory(
					).createClientHandler(null, toolkit);
		
		LogEvent[] results = client.retrieveLogEvents(0, 3);
		
		assertEquals(3, results.length);
		assertEquals("3", results[2].getMessage());
		
		results = client.retrieveLogEvents(3, 3);
		
		assertEquals(2, results.length);
		assertEquals("5", results[1].getMessage());
		
		results = client.retrieveLogEvents(5, 3);
		
		assertEquals(0, results.length);
		
		assertNull(archiver.l);
	}
	
	class ThirdServerSideToolkit extends MockServerSideToolkit {
		
		ConsoleArchiver archiver;
		
		@Override
		public ServerContext getContext() {
			return new MockServerContext() {
				@Override
				public ConsoleArchiver getConsoleArchiver() {
					return archiver;
				}
				@Override
				public ServerId getServerId() {
					return new ServerId("//test");
				}
			};
		}
	}
	
	class OurConsoleArchiver implements ConsoleArchiver {
		
		LogListener l;
		
		LogEvent[] logEvents = {
			new LogEvent("thing", 24L, LogLevel.DEBUG, "1"),
			new LogEvent("thing", 25L, LogLevel.DEBUG, "2"),
			new LogEvent("thing", 26L, LogLevel.DEBUG, "3"),
			new LogEvent("thing", 27L, LogLevel.DEBUG, "4"),
			new LogEvent("thing", 28L, LogLevel.DEBUG, "5")
		};
		
		public void addConsoleListener(LogListener l, Object component,
				long last, int max) {
			
			this.l = l;
			
			for (long seq = last; seq < 5 && seq < last + max; ++seq) {
				l.logEvent(logEvents[(int) seq]);
			}
		}
		
		public void removeConsoleListener(LogListener l, Object component) {
			this.l = null;
		}
		
		public String consoleIdFor(Object component) {
			return "apples";
		}
		
		public void onDestroy() {
			throw new RuntimeException("Unexpected.");
		}
	}
	
   @Test
	public void testRetrieveConsoleEvents() {
		
		OurConsoleArchiver archiver = new OurConsoleArchiver();
		
		ServerInterfaceHandlerFactory<Object, LogPollable> test = 
			new LogPollableHandlerFactory();
		
		ThirdServerSideToolkit serverKit = new ThirdServerSideToolkit();
		serverKit.archiver = archiver;
		
		ServerInterfaceHandler serverHandler = test.createServerHandler(
				null, serverKit);
		
		OurClientToolkit toolkit = new OurClientToolkit();
		toolkit.handler = serverHandler;
				
		LogPollable client = (LogPollable) 
			new LogPollableHandlerFactory.ClientFactory(
					).createClientHandler(null, toolkit);
		
		LogEvent[] results = client.retrieveConsoleEvents(0, 3);
		
		assertEquals(3, results.length);
		assertEquals("3", results[2].getMessage());
		
		results = client.retrieveConsoleEvents(3, 3);
		
		assertEquals(2, results.length);
		assertEquals("5", results[1].getMessage());
		
		results = client.retrieveConsoleEvents(5, 3);
		
		assertEquals(0, results.length);
		
		assertNull(archiver.l);
	}
}
