package org.oddjob.jmx.server;

import org.junit.Test;

import javax.management.MBeanOperationInfo;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.logging.LogEnabled;

import org.oddjob.OjTestCase;

public class JMXOperationFactoryTest extends OjTestCase {

   @Test
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
