/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.monitor.model.Describer;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.StateHandler;

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
		
		JobStateHandler stateHandler = new JobStateHandler(this);
		
		Object wrapped;
		MockWrapper(Object wrapped) {
			this.wrapped = wrapped;
		}
		
		@Override
		protected StateHandler<?> stateHandler() {
			return stateHandler;
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
		
		@Override
		public boolean softReset() {
			throw new RuntimeException("Unexpected.");
		}
		@Override
		public boolean hardReset() {
			throw new RuntimeException("Unexpected.");
		}
		
		@Override
		protected void fireDestroyedState() {
			throw new RuntimeException("Unexpected");
		}
	}

	/**
	 * Test getting a result.
	 * @throws ArooaConversionException 
	 * @throws ArooaPropertyException 
	 *
	 */
	public void testWithResult() throws ArooaPropertyException, ArooaConversionException {
		MockWrapper test = new MockWrapper(new Result());
		test.setArooaSession(new StandardArooaSession());
		assertEquals(42, test.getResult(null));
	}

	/**
	 * Test getting a result with no result.
	 * @throws ArooaConversionException 
	 * @throws ArooaPropertyException 
	 *
	 */
	public void testNoResult() throws ArooaPropertyException, ArooaConversionException {
		MockWrapper test = new MockWrapper(new Object());
		assertEquals(0, test.getResult(null));
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
