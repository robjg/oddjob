/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.monitor.model.Describer;

/**
 * Tests on BaseWrapper.
 *
 */
public class BaseWrapperTest extends TestCase {
	
	/** Bean fixture */
	public static class Result {
		public int getResult() {
			return 42;
		}
	}
	
	/** Test base wrapper by extending it. */
	class MockWrapper extends BaseWrapper {
		Object wrapped;
		MockWrapper(Object wrapped) {
			this.wrapped = wrapped;
		}
		public Object getWrapped() {
			return wrapped;
		}
		protected Object getProxy() {
			return null;
		}
		protected DynaBean getDynaBean() {
			return new WrapDynaBean(wrapped);
		}
		public void run() {}
	}

	/**
	 * Test getting a result.
	 *
	 */
	public void testWithResult() {
		MockWrapper test = new MockWrapper(new Result());
		assertEquals(42, test.getResult());
	}

	/**
	 * Test getting a result with no result.
	 *
	 */
	public void testNoResult() {
		MockWrapper test = new MockWrapper(new Object());
		assertEquals(0, test.getResult());
	}
	
	public static class MockBean {
		public String getReadable() {
			return "a";
		}
		public void setWritable(String writable) {
			
		}
		public String getBoth() {
			return "b";
		}
		public void setBoth(String both) {
			
		}
	}
	
	public void testDescribe() {
		MockWrapper test = new MockWrapper(new MockBean());
		
		Map<String, String> properties = Describer.describe(test);

		assertEquals("readable", "a", properties.get("readable"));
		assertEquals("both", "b", properties.get("both"));
		assertEquals("writable", null, properties.get("writable"));
	}
}
