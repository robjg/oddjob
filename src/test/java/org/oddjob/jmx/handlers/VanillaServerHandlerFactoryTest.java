package org.oddjob.jmx.handlers;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanOperationInfo;

import org.oddjob.OjTestCase;

import org.oddjob.jmx.client.LogPollable;

public class VanillaServerHandlerFactoryTest extends OjTestCase {

   @Test
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
