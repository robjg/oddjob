/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.framework.adapt.beanutil;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Test;
import org.oddjob.OjTestCase;

/**
 * Test WrapDynaBean.
 *
 */
public class WrapDynaBeanTest extends OjTestCase {

	/** Simple Bean fixture. */
	public static class SimpleBean {
		private String simple;
		public void setSimple(String simple) {
			this.simple = simple;
		}
		public String getSimple() {
			return simple;
		}
	}
	
   @Test
	public void testSimple() throws Exception {
		SimpleBean bean = new SimpleBean();
		
		WrapDynaBean wrap = new WrapDynaBean(bean);
		
		wrap.set("simple", "test");
		
		assertEquals("test", bean.getSimple());
		assertEquals("test", wrap.get("simple"));
		
		bean.setSimple(null);
		
		PropertyUtils.setProperty(wrap, "simple", "test");
		assertEquals("test", PropertyUtils.getProperty(wrap, "simple"));
	}
	
	public static class MappedBean {
		private Map<String, Object> map = new HashMap<String, Object>();
		public void setMapped(String name, Object value) {
			map.put(name, value);
		}
		public Object getMapped(String name) {
			return map.get(name);
		}
	}
	
   @Test
	public void testMapped() throws Exception {
		MappedBean bean = new MappedBean();
		
		WrapDynaBean wrap = new WrapDynaBean(bean);
		
		wrap.set("mapped", "simple", "test");
		
		assertEquals("test", bean.getMapped("simple"));
		assertEquals("test", wrap.get("mapped", "simple"));

		PropertyUtils.setProperty(wrap, "mapped(simple)", "test");
		assertEquals("test", PropertyUtils.getProperty(wrap, "mapped(simple)"));
	}
	
	public static class IndexedBean {
		private String[] array = new String[1];
		public void setIndexed(String[] array) {
			this.array = array;
		}
//		public void setIndexed(int index, String value) {
//			this.array[index] = value;
//		}
		public String[] getIndexed() {
			return array;
		}
//		public String getIndexed(int index) {
//			return array[index];
//		}
	}
	
   @Test
	public void testIndexed() throws Exception {
		IndexedBean bean = new IndexedBean();
		
		WrapDynaBean wrap = new WrapDynaBean(bean);
		
		wrap.set("indexed", 0, "test");
		
		assertEquals("test", bean.getIndexed()[0]);
		assertEquals("test", wrap.get("indexed", 0));

		PropertyUtils.setProperty(wrap, "indexed[0]", "test");
		assertEquals("test", PropertyUtils.getProperty(wrap, "indexed[0]"));
	}

	/** InAccessable Mapped Bean fixture. */
	public static class InAccessableBean {
		private String simple;
		public void setSimple(String simple) {
			this.simple = simple;
		}
		String getSimple() {
			return simple;
		}
	}
	
   @Test
	public void testInAccessable() throws Exception {
		InAccessableBean bean = new InAccessableBean();
		
		WrapDynaBean wrap = new WrapDynaBean(bean);
		
		wrap.set("simple", "test");
		
		assertEquals("test", bean.getSimple());
		
		// should behave like no read method and return null.
		assertEquals(null, wrap.get("simple"));
		
		bean.setSimple(null);
		
		PropertyUtils.setProperty(wrap, "simple", "test");
		
		// should behave like no read method and return null.
		assertEquals(null, PropertyUtils.getProperty(wrap, "simple"));
	}

	/** InAccessable Bean fixture. */
	public static class InAccessableMappedBean {
		private Map<String, Object> map = new HashMap<String, Object>();
		public void setMapped(String name, Object value) {
			map.put(name, value);
		}
		Object getMapped(String name) {
			return map.get(name);
		}
	}
	
   @Test
	public void testInAccessableMapped() throws Exception {
		InAccessableMappedBean bean = new InAccessableMappedBean();
		
		WrapDynaBean wrap = new WrapDynaBean(bean);
		
		wrap.set("mapped", "simple", "test");
		
		assertEquals("test", bean.getMapped("simple"));
		
		// should be null as read method not public
		assertEquals(null, wrap.get("mapped", "simple"));

		PropertyUtils.setProperty(wrap, "mapped(simple)", "test");
		
		// should be null as read method not public
		assertEquals(null, PropertyUtils.getProperty(wrap, "mapped(simple)"));
	}
}
