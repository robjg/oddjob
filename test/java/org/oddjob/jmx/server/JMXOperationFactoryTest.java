package org.oddjob.jmx.server;

import javax.management.MBeanOperationInfo;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.logging.LogEnabled;

import junit.framework.TestCase;

public class JMXOperationFactoryTest extends TestCase {

	public void testNoArgsOpInfo() throws SecurityException, NoSuchMethodException {
		
		JMXOperationFactory test = new JMXOperationFactory(LogEnabled.class);
		
		RemoteOperation<?> expected = new OperationInfoOperation(
				new MBeanOperationInfo("", 
						LogEnabled.class.getMethod("loggerName")));
		
		assertEquals(expected, test.operationFor(
				LogEnabled.class.getMethod("loggerName"),
				MBeanOperationInfo.INFO));
		
		assertEquals(expected, test.operationFor(
				LogEnabled.class.getMethod("loggerName"), 
					"Get's Log", MBeanOperationInfo.INFO));
		
		assertEquals(expected, test.operationFor(
				"loggerName", 
				MBeanOperationInfo.INFO));
		
		assertEquals(expected, test.operationFor(
				"loggerName", 
					"Get's Log", MBeanOperationInfo.INFO));
	}
}
