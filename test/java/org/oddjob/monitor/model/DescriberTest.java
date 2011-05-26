/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaMap;
import org.oddjob.arooa.beanutils.MagicBeanDefinition;
import org.oddjob.arooa.beanutils.MagicBeanProperty;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.framework.WrapDynaBean;

public class DescriberTest extends TestCase {

	public static class O1 {
		public Map<String, String> describe() {
			Map<String, String> map = new HashMap<String, String>();
			map.put("fruit", "apples");
			return map;
		}
	}

	public void test1() {
		Map<String, String> d = Describer.describe(new O1());
		assertEquals("apples", d.get("fruit"));
	}
	
	public static class O2 {
		public String getFruit() {
			return "apples";
		}
	}
	
	public void test2() {
		Map<String, String> d = Describer.describe(new O2());
		assertEquals("apples", d.get("fruit"));
	}

	public static class O3 {
		public void setFruit(String fruit) {
		}
	}	
	
	/**
	 * Bug in describe in beanutils for a dynabean without a read method. 
	 *
	 */
	public void testDynaBean() {
		WrapDynaBean wrap = new WrapDynaBean(new O3());
		Map<String, String> d = Describer.describe(wrap);
		// class is only property readable property
		// but fruit property still exists but is null.
		assertEquals(2, d.size());
		
	}
		
	/**
	 * Test with capital letter property. 
	 *
	 */
	public void testCapitalPropertiesDynaBean() {
		LazyDynaMap bean = new LazyDynaMap();
		
		bean.set("Fruit", "apple");
		
		Map<String, String> d = Describer.describe(bean);
		
		assertEquals(1, d.size());
		assertEquals("apple", d.get("Fruit"));
		
	}
	
	/**
	 * Test magic bean - tracking down a bug in JMXAttributes. 
	 *
	 */
	public void testMagicDynaBean() {
		MagicBeanDefinition def = new MagicBeanDefinition();
		def.setName("Foo");
		
		MagicBeanProperty prop0 = new MagicBeanProperty();
		prop0.setName("Fruit");
		prop0.setType(String.class.getName());
		
		def.setProperties(0, prop0);
		
		ArooaClass arooaClass = def.createMagic(getClass().getClassLoader());
		DynaBean bean = (DynaBean) arooaClass.newInstance();
		
		bean.set("Fruit", "apple");
		
		Map<String, String> d = Describer.describe(bean);
		
		assertEquals(1, d.size());
		assertEquals("apple", d.get("Fruit"));
		
	}
}
