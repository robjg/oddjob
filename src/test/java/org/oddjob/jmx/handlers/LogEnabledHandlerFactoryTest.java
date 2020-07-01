/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.handlers;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.server.*;
import org.oddjob.logging.LogEnabled;
import org.oddjob.util.MockThreadManager;

import static org.hamcrest.CoreMatchers.is;

public class LogEnabledHandlerFactoryTest extends OjTestCase {
//	private static final Logger logger = LoggerFactory.getLogger(IconicInfoTest.class);

	private static class MockLogEnabled implements LogEnabled {
		public String loggerName() {
			return "org.oddjob.TestLogger";
		}
	}
	
	private static class OurHierarchicalRegistry extends MockBeanRegistry {
		
		@Override
		public String getIdFor(Object component) {
			assertNotNull(component);
			return "x";
		}
		
	}
	
	private static class OurServerSession extends MockServerSession {
		
		ArooaSession session = new StandardArooaSession();
		
		@Override
		public ArooaSession getArooaSession() {
			return session;
		}
	}
	
    @Test
	public void testLoggerName() throws Exception {
		MockLogEnabled target = new MockLogEnabled();
		
		/// factory includes LogEnabledInfo by default
		ServerInterfaceManagerFactory imf = 
			new ServerInterfaceManagerFactoryImpl();
		
		ServerModel sm = new ServerModelImpl(
				new ServerId("//test/"), 
				new MockThreadManager(), 
				imf
				);
		
		ServerContext serverContext = new ServerContextImpl(
				target, sm, new OurHierarchicalRegistry());
		
		OddjobMBean ojmb = new OddjobMBean(
				target, 0,
				new OurServerSession(),
				serverContext);
		
		String loggerName = (String) ojmb.invoke("loggerName",
				new Object[0], new String[0]);
		
		assertEquals("logger name", "org.oddjob.TestLogger", loggerName);
	}
   
    @Test
    public void testEqualsAndHashCode() {
    	
    	LogEnabledHandlerFactory factory1 = new LogEnabledHandlerFactory();
    	LogEnabledHandlerFactory factory2 = new LogEnabledHandlerFactory();

    	assertThat(factory1.equals(factory2), is(true));    	
    	assertThat(factory1.hashCode(), is( factory2.hashCode()));    	
    }

}
