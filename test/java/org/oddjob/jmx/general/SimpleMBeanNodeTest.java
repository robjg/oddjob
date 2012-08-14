package org.oddjob.jmx.general;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.life.ClassLoaderClassResolver;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.log4j.Log4jArchiver;
import org.oddjob.script.ConvertableArguments;
import org.oddjob.script.InvokerArguments;

public class SimpleMBeanNodeTest extends TestCase {

	ObjectName objectName;
	
	MBeanServer mBeanServer;

	Vendor simple = new Vendor("Hay Medows");
	
	protected void setUp() throws Exception {
		objectName = new ObjectName("fruit:type=vendor,name=Pickles");
		
		mBeanServer = ManagementFactory.getPlatformMBeanServer();
		mBeanServer.registerMBean(simple, objectName);		
	}
	
	@Override
	protected void tearDown() throws Exception {
		mBeanServer.unregisterMBean(objectName);
	}
	
	public void testInvoking() throws Exception {
		
		SimpleMBeanNode test = new SimpleMBeanNode(
				objectName, mBeanServer, 
				new ClassLoaderClassResolver(getClass().getClassLoader()));
		
		InvokerArguments arguments = new ConvertableArguments(
				new DefaultConverter(), 
				"apples", "2012-08-01", 42);
		
		double result = (Double) test.invoke("quote", arguments);
		
		assertEquals(94.23, result, 0.001);
		assertEquals("apples", simple.fruit);
		assertEquals(DateHelper.parseDate("2012-08-01"), simple.delivery);
		assertEquals(42, simple.quantity);
		
	}
	
	public void testGetProperty() throws Exception {
		
		SimpleMBeanNode test = new SimpleMBeanNode(
				objectName, mBeanServer, 
				new ClassLoaderClassResolver(getClass().getClassLoader()));
		
		PropertyAccessor accessor = new BeanUtilsPropertyAccessor();
		
		String result = accessor.getProperty(test, "Farm", String.class);
		
		assertEquals("Hay Medows", result);
		
		accessor.setProperty(test, "Rating", 4.2);

		assertEquals(4.2, simple.rating, 0.01);
	}
	
	public void testLogEnabled() throws Exception {
		
		final StringBuilder builder = new StringBuilder();
		
		class TestListener implements LogListener {
			public void logEvent(LogEvent logEvent) {
				builder.append(logEvent.getMessage() + "\n");
			}
		}
		
		
		
		SimpleMBeanNode test = new SimpleMBeanNode(
				objectName, mBeanServer, 
				new ClassLoaderClassResolver(getClass().getClassLoader()));
		
		Log4jArchiver archiver = new Log4jArchiver(test, "%m");
		
		archiver.addLogListener(new TestListener(), test, 
				LogLevel.DEBUG, -1, 10000);
		
		test.initialise();
		
		System.out.println(builder.toString());
		
		assertTrue(builder.length() > 0);
	}
	
}
