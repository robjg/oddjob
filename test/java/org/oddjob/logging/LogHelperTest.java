/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.logging;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.LazyDynaBean;
import org.oddjob.Reserved;
import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;
import org.oddjob.arooa.reflect.ArooaPropertyException;

public class LogHelperTest extends TestCase {

	public static class AnyDynaBean implements DynaBean {
		DynaBean delegate = new LazyDynaBean();
		
		public boolean contains(String name, String key) {
			return delegate.contains(name, key);
		}
		public Object get(String name) {
			return delegate.get(name);
		}
		public Object get(String name, int index) {
			return delegate.get(name, index);
		}
		public Object get(String name, String key) {
			return delegate.get(name, key);
		}
		public DynaClass getDynaClass() {
			return delegate.getDynaClass();
		}
		public void remove(String name, String key) {
			delegate.remove(name, key);
		}
		public void set(String name, int index, Object value) {
			delegate.set(name, index, value);
		}
		public void set(String name, Object value) {
			delegate.set(name, value);
		}
		public void set(String name, String key, Object value) {
			delegate.set(name, key, value);
		}
		
		public String loggerName() {
			return "org.oddjob.TestLogger";
		}
	}
	
	public void testTheProblem() throws ArooaPropertyException {
		String loggerName = (String) new BeanUtilsPropertyAccessor().getProperty(
				new AnyDynaBean(), Reserved.LOGGER_PROPERTY);
		assertNull("can't get logger", loggerName);
	}
	
	class TheSolution extends AnyDynaBean implements LogEnabled {
		
	}
	
	public void testTheSolution() {
		String loggerName = LogHelper.getLogger(new TheSolution());
		assertEquals("logger name", "org.oddjob.TestLogger", loggerName);
	}
	
}
