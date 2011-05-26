package org.oddjob.jmx.handlers;

import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanOperationInfo;

import junit.framework.TestCase;

import org.oddjob.jmx.client.LogPollable;

public class VanillaServerHandlerFactoryTest extends TestCase {

	public void testOpInfoForClass() {
		
		VanillaServerHandlerFactory<LogPollable> test = 
			new VanillaServerHandlerFactory<LogPollable>(LogPollable.class);
		
		MBeanOperationInfo[] results = test.getMBeanOperationInfo();
		
		assertEquals(4, results.length);
		
		Set<String> set = new HashSet<String>();

		for (MBeanOperationInfo result : results) {
			set.add(result.getName());
		}
		
		assertTrue(set.contains("url"));		
	}
}
